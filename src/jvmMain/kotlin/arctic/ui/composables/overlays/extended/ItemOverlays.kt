package arctic.ui.composables.overlays.extended

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import arctic.states.Arctic
import arctic.states.EditorState
import arctic.states.ItemCreationOverlayState
import arctic.ui.composables.atomic.ItemRarityButton
import arctic.ui.composables.atomic.PowerEditField
import arctic.ui.composables.atomic.drawUniqueIndicator
import arctic.ui.composables.overlays.OverlayBackdrop
import arctic.ui.composables.overlays.SizeMeasureDummy
import arctic.ui.utils.rememberMutableInteractionSource
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.*
import dungeons.states.ArmorProperty
import dungeons.states.Item
import dungeons.states.extensions.addItem
import extensions.padEndRemaining
import extensions.toFixed


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ItemCreationOverlay(editorState: EditorState) {
    val itemCreationOverlay = Arctic.overlayState.itemCreation

    OverlayBackdrop(itemCreationOverlay != null) { Arctic.overlayState.itemCreation = null }
    AnimatedContent(
        targetState = itemCreationOverlay,
        transitionSpec = { fadeIn() with fadeOut() using SizeTransform(false) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (it != null) ItemCreationOverlayContent(it, editorState)
        else SizeMeasureDummy()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ItemCreationOverlayContent(itemCreationOverlay: ItemCreationOverlayState, editorState: EditorState) {
    val blurRadius by animateDpAsState(if (itemCreationOverlay.preview != null) 75.dp else 0.dp)
    val variantFilter = remember { mutableStateOf("Melee") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()/*.blur(blurRadius)*/.graphicsLayer { renderEffect = if (blurRadius != 0.dp) BlurEffect(blurRadius.value, blurRadius.value) else null }
    ) {
        Box {
            ItemCreationVariantFilters(fixed = null, filter = variantFilter, modifier = Modifier.offset(x = (-180).dp))
            AnimatedContent(
                targetState = variantFilter.value,
                transitionSpec = {
                    val enter = fadeIn() + slideIn { IntOffset(30.dp.value.toInt(), 0) }
                    val exit = fadeOut() + slideOut { IntOffset(30.dp.value.toInt(), 0) }
                    enter with exit using SizeTransform(false)
                }
            ) { variant ->
                ItemDataCollection(variant, onItemSelect = { itemCreationOverlay.preview = it })
            }
        }
    }

    ItemDataDetailOverlay(itemCreationOverlay, editorState)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ItemEditionOverlay() {
    val itemEditionOverlay = Arctic.overlayState.itemEdition

    OverlayBackdrop(itemEditionOverlay != null) { Arctic.overlayState.itemEdition = null }
    AnimatedContent(
        targetState = itemEditionOverlay,
        transitionSpec = { fadeIn() with fadeOut() using SizeTransform(false) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (it != null) ItemEditionOverlayContent(it)
        else SizeMeasureDummy()
    }
}

@Composable
fun ItemEditionOverlayContent(itemEditionTarget: Item) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box {
            ItemCreationVariantFilters(fixed = itemEditionTarget.data.variant, filter = null, modifier = Modifier.offset(x = (-180).dp))
            ItemDataCollection(
                variant = itemEditionTarget.data.variant,
                onItemSelect = { itemEditionTarget.type = it.type; Arctic.overlayState.itemEdition = null }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ItemDataDetailOverlay(itemCreationOverlay: ItemCreationOverlayState, editorState: EditorState) {
    OverlayBackdrop(itemCreationOverlay.preview != null) { itemCreationOverlay.preview = null }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedContent(
            targetState = itemCreationOverlay.preview,
            transitionSpec = OverlayTransitions.detail(),
            modifier = Modifier.fillMaxWidth().height(550.dp)
        ) { target ->
            if (target != null) ItemDataDetail(target, editorState)
            else Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ItemCreationVariantFilters(fixed: String?, filter: MutableState<String>?, modifier: Modifier = Modifier) {
    val items = listOf("Melee", "Ranged", "Armor", "Artifact")
    Column(horizontalAlignment = Alignment.End, modifier = Modifier.requiredWidth(160.dp).padding(top = 58.dp).then(modifier)) {
        for (item in items) {
            ItemCreationVariantFilter(Localizations.UiText(item.lowercase()), item, filter, fixed == null || fixed == item)
        }
    }
}

@Composable
private fun ItemCreationVariantFilter(text: String, variant: String, filter: MutableState<String>?, enabled: Boolean = true) {
    val selected = filter?.value == variant
    val onClick = { filter?.value = variant }

    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val blurAlpha by animateFloatAsState(if (!enabled) 0f else if (hovered) 0.8f else 0f)
    val overlayAlpha by animateFloatAsState(if (!enabled) 0.15f else if (selected) 1f else 0.6f)

    Box(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .hoverable(interaction, enabled = enabled)
            .clickable(interaction, null, enabled = enabled, onClick = onClick)
    ) {
        VariantFilterText(text, modifier = Modifier.blur(10.dp).graphicsLayer { alpha = blurAlpha })
        VariantFilterText(text, modifier = Modifier.graphicsLayer { alpha = overlayAlpha })
    }
}

@Composable
private fun VariantFilterText(text: String, modifier: Modifier = Modifier) =
    Text(
        text = text,
        color = Color.White,
        fontSize = 35.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 20.dp, top = 5.dp, bottom = 5.dp, end = 20.dp)
    )


private const val Columns = 5

@Composable
private fun ItemDataCollection(variant: String, onItemSelect: (ItemData) -> Unit) {
    val collection = remember(variant) {
        Database.items
            .filter { it.variant == variant }
            .sortedBy { "${if (it.unique) 0 else 1}${it.type.replace(Regex("_.+"), "")}_${it.name}" }
            .padEndRemaining(Columns) { null }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(Columns),
        contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
        modifier = Modifier
            .fillMaxHeight()
            .requiredWidth(950.dp)
            .background(Color(0xff080808))
            .clickable(rememberMutableInteractionSource(), null) { }
            .padding(horizontal = 20.dp)
    ) {
        item(span = { GridItemSpan(5) }) { CollectionCategoryHeader(variant) }
        items(collection) { ItemDataIcon(it, onItemSelect) }
    }
}

@Composable
private fun CollectionCategoryHeader(variant: String) {
    val category = remember(variant) { Localizations.UiText(variant.lowercase()) }
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .height(115.dp)
            .padding(start = 20.dp, bottom = 15.dp)
    ) {
        Text(text = category, color = Color.White, fontSize = 35.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ItemDataIcon(data: ItemData?, onItemSelect: (ItemData) -> Unit) {
    if (data == null) return

    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val behindBlurAlpha by animateFloatAsState(if (hovered) 1f else 0f)

    val DrawUniqueIndicatorModifier = Modifier
        .rotate(-20f)
        .drawBehind { drawUniqueIndicator() }
        .rotate(20f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .hoverable(interaction)
            .clickable(interaction, null) { onItemSelect(data) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / 1f)
                .padding(20.dp)
                .then(if (data.unique) DrawUniqueIndicatorModifier else Modifier)
        ) {
            ItemDataIconImage(data, modifier = Modifier.scale(1.1f).graphicsLayer { alpha = behindBlurAlpha; renderEffect = BlurEffect(10.dp.value, 10.dp.value) })
            ItemDataIconImage(data)
        }
        Text(
            text = data.name ?: Localizations.UiText("unknown_item"),
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.offset(y = (-10).dp)
        )
    }
}

@Composable
private fun ItemDataIconImage(data: ItemData, modifier: Modifier = Modifier) =
    Image(data.inventoryIcon, null, modifier = Modifier.fillMaxSize().then(modifier))

@Composable
fun ItemDataDetail(data: ItemData, editorState: EditorState) {
    var rarity by remember { mutableStateOf(if (data.unique) "Unique" else "Common") }
    var power by remember { mutableStateOf(DungeonsPower.toSerializedPower(editorState.stored.playerPower.toDouble())) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(385.dp)
            .background(Color(0xff080808))
            .clickable(rememberMutableInteractionSource(), null) { }
            .padding(40.dp)
    ) {
        Box(
            contentAlignment = Alignment.TopEnd,
            modifier = Modifier.requiredWidth(950.dp)
        ) {
            Row {
                Image(
                    bitmap = data.largeIcon,
                    contentDescription = null,
                    modifier = Modifier.fillMaxHeight().aspectRatio(1f / 1f)
                )
                Spacer(modifier = Modifier.width(25.dp))
                Column {
                    ItemRarityButton(data, rarity) { rarity = it }
                    Text(
                        text = data.name ?: Localizations.UiText("unknown_item"),
                        color = Color.White,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val description = data.description
                    if (description != null)
                        Text(text = description, color = Color.White, fontSize = 25.sp)

                    val flavour = data.flavour
                    if (flavour != null)
                        Text(text = flavour, color = Color.White, fontSize = 25.sp)

                    Spacer(modifier = Modifier.weight(1f))

                    PowerEditField(
                        value = DungeonsPower.toInGamePower(power).toFixed(6).toString(),
                        onValueChange = {
                            if (it.toDoubleOrNull() != null) power = DungeonsPower.toSerializedPower(it.toDouble())
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.offset(y = 365.dp)) {
                if (data.variant != "Artifact") WarningText(text = Localizations.UiText("item_creation_other_options_description"))
                if (data.variant == "Armor") WarningText(text = Localizations.UiText("item_creation_armor_property_description"))
            }
        }
        AddButton {
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
            Arctic.overlayState.itemCreation = null
        }
    }
}

@Composable
private fun WarningText(text: String) {
    Text(text = text, color = Color.White.copy(alpha = 0.45f), fontSize = 18.sp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoxScope.AddButton(onClick: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val hoverAlpha by animateFloatAsState(if (hovered) 0.8f else 0f)
    val baseAlpha by animateFloatAsState(if (hovered) 0.8f else 0.7f)

    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(x = (-100).dp)
            .hoverable(interaction)
            .onClick(onClick = onClick)
            .graphicsLayer { clip = false }
    ) {
        Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            AddButtonIcon(modifier = Modifier.blur(10.dp).graphicsLayer { alpha = hoverAlpha })
            AddButtonIcon(modifier = Modifier.graphicsLayer { alpha = baseAlpha })
        }
        Text(
            text = Localizations.UiText("add"),
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 25.dp)
                .graphicsLayer { alpha = baseAlpha }
        )
    }
}

@Composable
private fun AddButtonIcon(modifier: Modifier = Modifier) {
    Image(
        bitmap = IngameImages.get { "/Game/UI/Materials/Character/right_arrow_carousel.png" },
        contentDescription = null,
        filterQuality = FilterQuality.None,
        modifier = modifier.then(Modifier.size(125.dp).padding(12.5.dp))
    )
}
