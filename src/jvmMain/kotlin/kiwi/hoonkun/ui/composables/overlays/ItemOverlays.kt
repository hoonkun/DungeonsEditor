package kiwi.hoonkun.ui.composables.overlays

import LocalWindowState
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.*
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.ArmorProperty
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsDatabase
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.resources.ItemData
import minecraft.dungeons.values.DungeonsPower


private val ItemVariants = listOf("Melee", "Ranged", "Armor", "Artifact")

@Composable
fun ItemCreationOverlay(
    editorState: EditorState,
    requestClose: () -> Unit
) {
    val overlays = LocalOverlayState.current
    val density = LocalDensity.current

    var variantFilter by remember { mutableStateOf(ItemVariants[0]) }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        ItemCreationVariantFilters(
            filter = variantFilter,
            onFilterChange = { variantFilter = it },
            modifier = Modifier.offset((-850).dp / 2).offsetRelative(x = -0.5f)
        )
        AnimatedContent(
            targetState = variantFilter,
            transitionSpec = {
                val offset =
                    if (ItemVariants.indexOf(targetState) > ItemVariants.indexOf(initialState)) 30.dp
                    else (-30).dp

                val enter = fadeIn() + slideIn { with (density) { IntOffset(0, offset.roundToPx()) } }
                val exit = fadeOut() + slideOut { with (density) { IntOffset(0, -offset.roundToPx()) } }

                enter togetherWith  exit using SizeTransform(false)
            },
            modifier = ItemDataCollectionDefaultModifier
        ) { variant ->
            ItemDataCollection(
                variant = variant,
                onItemSelect = { selected ->
                    overlays.make(
                        enter = defaultFadeIn() + slideIn { with (density) { IntOffset(0, 50.dp.roundToPx()) } },
                        exit = defaultFadeOut()
                    ) { id ->
                        ItemDataDetailOverlay(
                            data = selected,
                            editorState = editorState,
                            onSubmit = {
                                overlays.destroy(id)
                                requestClose()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

val ItemDataCollectionDefaultModifier = Modifier
    .fillMaxHeight()
    .requiredWidth(850.dp)
    .background(Color(0xff080808))

@Composable
fun ItemEditionOverlay(
    target: Item,
    requestClose: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        ItemDataCollection(
            variant = target.data.variant,
            onItemSelect = {
                target.type = it.type
                requestClose()
            },
            modifier = ItemDataCollectionDefaultModifier
        )
    }
}

@Composable
private fun ItemDataDetailOverlay(
    data: ItemData,
    editorState: EditorState,
    onSubmit: () -> Unit,
) {
    val windowState = LocalWindowState.current

    var rarity by remember { mutableStateOf(if (data.unique) "Unique" else "Common") }
    var power by remember { mutableStateOf(DungeonsPower.toSerializedPower(editorState.stored.playerPower.toDouble())) }

    val scroll = rememberScrollState()
    val adapter = rememberScrollbarAdapter(scroll)

    val verticalMargin = (windowState.size.height - 425.dp) / 2f
    val horizontalContentPadding = (windowState.size.width - 950.dp) / 2f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = verticalMargin)
            .background(Color(0xff080808))
            .consumeClick()
            .padding(vertical = 40.dp, horizontal = horizontalContentPadding)
    ) {
        Row {
            Image(
                bitmap = data.largeIcon,
                contentDescription = null,
                modifier = Modifier.fillMaxHeight().aspectRatio(1f / 1f)
            )
            Column(
                modifier = Modifier.padding(start = 25.dp)
            ) {
                ItemRarityButton(data, rarity) { rarity = it }
                AutosizeText(
                    text = data.name,
                    style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                    maxFontSize = 50.sp,
                )

                Box(modifier = Modifier.requiredHeightIn(max = 150.dp)) {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll)) {
                        val description = data.description
                        if (description != null)
                            Text(text = description, color = Color.White, fontSize = 25.sp)

                        val flavour = data.flavour
                        if (flavour != null)
                            Text(text = flavour, color = Color.White, fontSize = 25.sp)
                    }
                    VerticalScrollbar(adapter, modifier = Modifier.align(Alignment.TopEnd))
                }

                Spacer(modifier = Modifier.weight(1f))

                PowerEditField(
                    power = DungeonsPower.toInGamePower(power),
                    onPowerChange = { power = DungeonsPower.toSerializedPower(it) },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 365.dp)
        ) {
            if (data.variant != "Artifact")
                WarningText(text = Localizations.UiText("item_creation_other_options_description"))
            if (data.variant == "Armor")
                WarningText(text = Localizations.UiText("item_creation_armor_property_description"))
        }

        AddButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .graphicsLayer { clip = false }
                .offset(x = horizontalContentPadding / 2f)
                .offsetRelative(x = 0.5f),
            onClick = {
                val newItem = Item(
                    parent = editorState.stored,
                    inventoryIndex = 0,
                    power = power,
                    rarity = rarity,
                    type = data.type,
                    upgraded = false,
                    enchantments = if (data.variant != "Artifact") listOf() else null,
                    armorProperties = null,
                    markedNew = true
                )
                if (data.variant == "Armor")
                    newItem.armorProperties = data.builtInProperties
                        .map { ArmorProperty(holder = newItem, id = it.id, rarity = "Common") }
                        .toMutableStateList()

                newItem.parent.addItem(editorState, newItem)
                onSubmit()
            }
        )
    }
}

@Composable
private fun ItemCreationVariantFilters(
    filter: String,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .padding(top = 58.dp)
            .then(modifier)
    ) {
        for (item in ItemVariants) {
            ItemCreationVariantFilter(Localizations.UiText(item.lowercase()), item, filter, onFilterChange)
        }
    }
}

@Composable
private fun ItemCreationVariantFilter(
    text: String,
    variant: String,
    filter: String,
    onFilterChange: (String) -> Unit,
) {
    val density = LocalDensity.current

    val selected = filter == variant
    val onClick = { onFilterChange(variant) }

    val offset by animateDpAsState(if (selected) 5.dp else 50.dp)

    RetroButton(
        color = Color(0xff454545),
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        radius = RetroButtonDpCornerRadius(
            topStart = 8.dp,
            topEnd = 0.dp,
            bottomEnd = 0.dp,
            bottomStart = 8.dp
        ),
        contentPadding = PaddingValues(end = 50.dp, start = 20.dp),
        contentArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(bottom = 16.dp)
            .size(245.dp, 65.dp)
            .offset { with (density) { IntOffset(offset.roundToPx(), 0.dp.roundToPx()) } },
        onClick = onClick
    ) {
        Image(
            bitmap = VariantFilterIcon(with = variant, selected = true),
            contentDescription = null,
            modifier = Modifier.size(35.dp)
        )
        Text(
            text = text,
            style = LocalTextStyle.current.copy(fontSize = 24.sp),
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}

private const val Columns = 4

@Composable
private fun ItemDataCollection(
    variant: String,
    onItemSelect: (ItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    val collection = remember(variant) {
        DungeonsDatabase.items
            .filter { it.variant == variant }
            .sortedBy { "${if (it.unique) 0 else 1}${it.type.replace(Regex("_.+"), "")}_${it.name}" }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(Columns),
        contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
        modifier = modifier
            .consumeClick()
            .padding(horizontal = 20.dp)
    ) {
        item(span = { GridItemSpan(Columns) }) { CollectionCategoryHeader(variant) }
        items(collection) { ItemDataIcon(it, onItemSelect) }
    }
}

@Composable
private fun CollectionCategoryHeader(variant: String) =
    Text(
        text = Localizations.UiText(variant.lowercase()),
        fontSize = 35.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 62.dp, start = 20.dp, bottom = 15.dp)
    )

@Composable
private fun ItemDataIcon(data: ItemData, onItemSelect: (ItemData) -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .hoverable(interaction)
            .clickable(interaction, null) { onItemSelect(data) }
    ) {
        BlurShadowImage(
            bitmap = data.inventoryIcon,
            enabled = hovered,
            contentPadding = PaddingValues(all = 20.dp),
            onDrawBehind = { _, _ -> if (data.unique) drawUniqueIndicator() },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / 1f)
        )
        Text(
            text = data.name,
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = (-10).dp)
        )
    }
}

@Composable
private fun WarningText(text: String) =
    Text(text = text, color = Color.White.copy(alpha = 0.45f), fontSize = 18.sp)

@Composable
private fun AddButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    RetroButton(
        color = Color(0xff3f8e4f),
        modifier = Modifier.size(width = 175.dp, height = 60.dp).then(modifier),
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        onClick = onClick,
    ) {
        Text(
            text = Localizations.UiText("add"),
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 24.dp)
        )
        Image(
            bitmap = DungeonsTextures["/Game/UI/Materials/Character/right_arrow_carousel.png"],
            contentDescription = null,
            filterQuality = FilterQuality.None,
            modifier = Modifier.size(28.dp)
        )
    }
}