package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.*
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsSkeletons
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.skeleton
import minecraft.dungeons.states.extensions.withItemManager
import minecraft.dungeons.values.DungeonsItem
import minecraft.dungeons.values.DungeonsPower


@Stable
sealed interface ItemOverlayState {
    var selected: DungeonsSkeletons.Item?

    val variant: DungeonsItem.Variant
    val collection: List<DungeonsSkeletons.Item>
}

@Stable
class ItemOverlayCreateState(val editorState: EditorState): ItemOverlayState {
    override var selected: DungeonsSkeletons.Item? by mutableStateOf(null)

    override var variant by mutableStateOf(DungeonsItem.Variant.entries[0])
    override val collection: List<DungeonsSkeletons.Item> by derivedStateOf {
        DungeonsSkeletons.Item[Unit]
            .filter { it.variant == variant }
            .sortedBy { "${if (it.unique) 0 else 1}_${it.name}_${it.type.replace(Regex("_.+"), "")}" }
    }
}

@Stable
class ItemOverlayEditState(val target: MutableDungeons.Item): ItemOverlayState {
    override var selected: DungeonsSkeletons.Item? by mutableStateOf(null)

    override val variant: DungeonsItem.Variant = target.skeleton.variant
    override val collection: List<DungeonsSkeletons.Item> =
        DungeonsSkeletons.Item[Unit]
            .filter { it.variant == variant }
            .sortedBy { "${if (it.unique) 0 else 1}_${it.name}_${it.type.replace(Regex("_.+"), "")}" }
}

@Composable
fun AnimatedVisibilityScope?.ItemOverlay(
    state: ItemOverlayState,
    requestClose: () -> Unit
) {
    val density = LocalDensity.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .minimizableAnimateEnterExit(
                scope = this@ItemOverlay,
                enter = minimizableEnterTransition { fadeIn() + slideIn { with(density) { IntOffset(0, 45.dp.roundToPx()) } } }
            )
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            if (state is ItemOverlayCreateState) {
                ItemCreationVariantFilters(
                    filter = state.variant,
                    onFilterChange = { state.variant = it },
                    modifier = Modifier.offset(ItemDataCollectionWidth / 2).offsetRelative(x = 0.5f)
                )
            }
            MinimizableAnimatedContent(
                targetState = state.variant to state.collection,
                transitionSpec = minimizableContentTransform spec@ {
                    val previousIndex = DungeonsItem.Variant.entries.indexOf(initialState.first)
                    val nextIndex = DungeonsItem.Variant.entries.indexOf(targetState.first)

                    val offset =
                        if (nextIndex > previousIndex) 30.dp
                        else (-30).dp

                    val enter = fadeIn() + slideIn { with(density) { IntOffset(0, offset.roundToPx()) } }
                    val exit = fadeOut() + slideOut { with(density) { IntOffset(0, -offset.roundToPx()) } }

                    enter togetherWith exit using SizeTransform(false)
                },
                modifier = ItemDataCollectionDefaultModifier
            ) { (_, collection) ->
                ItemDataCollection(
                    collection = collection,
                    state = state,
                    onItemSelect = { state.selected = if (it == state.selected) null else it },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        MinimizableAnimatedContent(
            targetState = state.selected,
            transitionSpec = minimizableContentTransform spec@ {
                fadeIn() togetherWith fadeOut() using SizeTransform(false)
            },
            contentKey = { it != null },
            modifier = Modifier.offset(y = (60 - 48).dp / 2f)
        ) {
            if (it != null) {
                ItemDataDetail(
                    data = it,
                    state = state,
                    postSubmit = { requestClose() }
                )
            } else {
                Spacer(modifier = Modifier.height(ItemDetailHeight))
            }
        }
    }
}

val ItemDetailWidth get() = 800.dp
val ItemDetailHeight get() = 400.dp

val ItemDataCollectionWidth get() = 600.dp
val ItemDataCollectionDefaultModifier get() = Modifier
    .fillMaxHeight()
    .requiredWidth(ItemDataCollectionWidth)
    .background(Color(0xff080808))

@Composable
private fun ItemDataDetail(
    data: DungeonsSkeletons.Item,
    state: ItemOverlayState,
    postSubmit: () -> Unit,
) {
    val density = LocalDensity.current

    var rarity by remember {
        mutableStateOf(
            if (data.unique) DungeonsItem.Rarity.Unique
            else DungeonsItem.Rarity.Common
        )
    }
    var power by remember {
        mutableStateOf(
            if (state is ItemOverlayCreateState)
                state.editorState.data.playerPower.toDouble()
            else
                0.0
        )
    }

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(start = 32.dp)
            .requiredSize(ItemDetailWidth, ItemDetailHeight)
            .consumeClick()
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .align(Alignment.Start)
                .height(48.dp)
        ) Descriptions@ {
            if (state is ItemOverlayEditState) return@Descriptions
            if (data.variant != DungeonsItem.Variant.Artifact)
                WarningText(text = Localizations["item_creation_other_options_description"])
            if (data.variant == DungeonsItem.Variant.Armor)
                WarningText(text = Localizations["item_creation_armor_property_description"])
        }

        MinimizableAnimatedContent(
            targetState = data,
            transitionSpec = minimizableContentTransform spec@ {
                val a = if (state.collection.indexOf(initialState) > state.collection.indexOf(targetState)) (-50).dp else 50.dp
                val b = a * -1

                val enter = fadeIn() + slideIn(spring(stiffness = Spring.StiffnessLow)) { with(density) { IntOffset(0, a.roundToPx()) } }
                val exit = fadeOut() + slideOut(spring(stiffness = Spring.StiffnessLow)) { with(density) { IntOffset(0, b.roundToPx()) } }

                enter togetherWith exit
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .weight(1f)
                .background(Color(0xff080808))
                .padding(all = 16.dp)
        ) { capturedData ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    bitmap = capturedData.largeIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f / 1f)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 25.dp, end = 16.dp)
                        .padding(vertical = 16.dp)
                        .weight(1f)
                ) {
                    Row {
                        ItemRarityButton(capturedData, rarity) { rarity = it }
                        capturedData.appliedExclusiveEnchantment?.let {
                            Spacer(modifier = Modifier.width(8.dp))
                            BuiltInEnchantments(it)
                        }
                    }
                    AutosizeText(
                        text = capturedData.name,
                        style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                        maxFontSize = 40.sp,
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.fillMaxHeight().verticalScroll(scroll)) {
                            val description = capturedData.description
                            if (description != null)
                                Text(text = description, fontSize = 18.sp)

                            val flavour = capturedData.flavour
                            if (flavour != null)
                                Text(text = flavour, fontSize = 18.sp)
                        }
                        AutoHidingVerticalScrollbar(scrollState = scroll, modifier = Modifier.align(Alignment.TopEnd))
                    }

                    if (state is ItemOverlayCreateState) {
                        PowerEditField(
                            power = power,
                            onPowerChange = { power = it },
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        AddButton(
            text = if (state is ItemOverlayCreateState) Localizations["add"] else Localizations["replace"],
            modifier = Modifier.align(Alignment.End),
            onClick = {
                when (state) {
                    is ItemOverlayCreateState -> {
                        val newItem = MutableDungeons.Item(
                            inventoryIndex = 0,
                            power = DungeonsPower.toSerializedPower(power),
                            rarity = rarity,
                            type = data.type,
                            upgraded = false,
                            enchantments = if (data.variant != DungeonsItem.Variant.Artifact) listOf() else null,
                            armorProperties = null,
                            markedNew = true
                        )
                        if (data.variant == DungeonsItem.Variant.Armor) {
                            newItem.armorProperties.clear()
                            newItem.armorProperties.addAll(
                                data.builtInProperties
                                    .map { MutableDungeons.ArmorProperty(id = it.id) }
                                    .toMutableStateList()
                            )
                        }

                        val editor = state.editorState

                        val created = withItemManager {
                            editor.data.add(
                                newItem = newItem,
                                where = editor.view
                            )
                        }

                        editor.deselectAll()
                        editor.select(
                            item = created,
                            into = EditorState.Slot.Primary
                        )
                    }
                    is ItemOverlayEditState -> {
                        state.target.type = data.type
                    }
                }
                postSubmit()
            }
        )
    }
}

@Composable
private fun ItemCreationVariantFilters(
    filter: DungeonsItem.Variant,
    onFilterChange: (DungeonsItem.Variant) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .padding(top = 58.dp)
            .then(modifier)
    ) {
        for (item in DungeonsItem.Variant.entries) {
            ItemCreationVariantFilter(
                text = Localizations[item.name.lowercase()],
                variant = item,
                filter = filter,
                onFilterChange = onFilterChange
            )
        }
    }
}

@Composable
private fun ItemCreationVariantFilter(
    text: String,
    variant: DungeonsItem.Variant,
    filter: DungeonsItem.Variant,
    onFilterChange: (DungeonsItem.Variant) -> Unit,
) {
    val selected = filter == variant
    val onClick = { onFilterChange(variant) }

    val color by minimizableAnimateColorAsState(
        targetValue = if (selected) Color(0xff3f8e4f) else Color(0xff454545),
        animationSpec = minimizableSpec { spring() }
    )

    RetroButton(
        color = { color },
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        radius = RetroButtonDpCornerRadius(
            topStart = 0.dp,
            topEnd = 8.dp,
            bottomEnd = 8.dp,
            bottomStart = 0.dp
        ),
        contentPadding = PaddingValues(start = 10.dp, end = 24.dp),
        contentArrangement = Arrangement.End,
        modifier = Modifier
            .padding(bottom = 16.dp)
            .size(185.dp, 65.dp)
            .offset(x = (-5).dp),
        onClick = onClick
    ) {
        Text(
            text = text,
            style = LocalTextStyle.current.copy(fontSize = 24.sp),
            modifier = Modifier.padding(end = 20.dp)
        )
        Image(
            bitmap = VariantFilterIcon(with = variant, selected = true),
            contentDescription = null,
            modifier = Modifier.size(25.dp)
        )
    }
}

@Composable
private fun ItemDataCollection(
    collection: List<DungeonsSkeletons.Item>,
    state: ItemOverlayState,
    onItemSelect: (DungeonsSkeletons.Item) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 40.dp),
        modifier = modifier.fillMaxSize().consumeClick()
    ) {
        items(collection, key = { it.type }) {
            ItemDataItem(data = it, selected = it == state.selected, onItemSelect = onItemSelect)
        }
    }
}

@Composable
private fun ItemDataItem(
    data: DungeonsSkeletons.Item,
    selected: Boolean,
    onItemSelect: (DungeonsSkeletons.Item) -> Unit
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interaction)
            .clickable(interaction, null) { onItemSelect(data) }
            .clipToBounds()
            .drawBehind {
                if (selected) drawRect(Color.White.copy(alpha = 0.15f))
                if (hovered) drawRect(Color.White.copy(alpha = 0.1f))
            }
            .padding(vertical = 10.dp, horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier.size(100.dp)
        ) {
            Image(
                bitmap = data.inventoryIcon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().scale(1.6f).alpha(0.2f).offsetRelative(x = -0.15f)
            )
            Image(
                bitmap = data.inventoryIcon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = data.name,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.requiredHeight(35.dp)
            ) {
                if (data.unique || data.limited) {
                    ItemRarityButton(data, DungeonsItem.Rarity.Unique)
                } else {
                    ItemRarityButton(data, rarity = DungeonsItem.Rarity.Rare, readonly = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    ItemRarityButton(data, rarity = DungeonsItem.Rarity.Common, readonly = true)
                }
                data.appliedExclusiveEnchantment?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    BuiltInEnchantments(it)
                }
            }
        }
    }
}

@Composable
private fun WarningText(text: String) =
    Text(text = text, color = Color.White.copy(alpha = 0.45f), fontSize = 18.sp)

@Composable
private fun AddButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    RetroButton(
        color = Color(0xff3f8e4f),
        modifier = Modifier.size(width = 200.dp, height = 60.dp).then(modifier),
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        onClick = onClick,
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 24.dp)
        )
        Image(
            bitmap = DungeonsTextures["/UI/Materials/Character/right_arrow_carousel.png"],
            contentDescription = null,
            filterQuality = FilterQuality.None,
            modifier = Modifier.size(28.dp)
        )
    }
}