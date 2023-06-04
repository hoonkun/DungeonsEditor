package arctic.ui.composables.overlays.extended

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import arctic.states.arctic
import arctic.ui.composables.atomic.RarityColor
import arctic.ui.composables.atomic.RarityColorType
import arctic.ui.composables.atomic.ItemRarityButton
import arctic.ui.composables.atomic.PowerEditField
import arctic.ui.composables.atomic.drawUniqueIndicator
import arctic.ui.composables.overlays.OverlayBackdrop
import arctic.ui.utils.rememberMutableInteractionSource
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.*
import dungeons.states.ArmorProperty
import dungeons.states.Item
import dungeons.states.extensions.addItem
import dungeons.states.extensions.data
import dungeons.states.extensions.playerPower
import extensions.padEndRemaining
import extensions.toFixed


@Composable
fun ItemCreationOverlay() {
    ItemDataCollectionOverlay(
        targetState = arctic.creation.enabled.takeIf { it },
        variant = { arctic.creation.filter },
        blur = arctic.creation.target != null,
        onExit = { arctic.creation.disable() },
        onSelect = { arctic.creation.target = it }
    )

    ItemDataDetailOverlay(
        target = arctic.creation.target
    )
}

@Composable
fun ItemEditionOverlay() {
    ItemDataCollectionOverlay(
        targetState = arctic.edition.target,
        variant = { it.data.variant },
        onExit = { arctic.edition.disable() },
        onSelect = {
            arctic.edition.target?.type = it.type
            arctic.edition.disable()
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S>ItemDataCollectionOverlay(
    targetState: S?,
    variant: (S) -> String,
    filterEnabled: Boolean = true,
    blur: Boolean = false,
    onExit: () -> Unit,
    onSelect: (ItemData) -> Unit
) {
    val enabled = targetState != null
    val blurRadius by animateDpAsState(if (blur) 75.dp else 0.dp)

    OverlayBackdrop(enabled, onClick = onExit)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().blur(blurRadius)
    ) {
        AnimatedContent(
            targetState = targetState,
            transitionSpec = OverlayTransitions.collection(),
            modifier = Modifier.width(160.dp).padding(top = 54.dp)
        ) { targetState ->
            if (targetState != null) ItemCreationVariantFilters(if (filterEnabled) null else variant(targetState))
            else Spacer(modifier = Modifier.requiredWidth(160.dp).fillMaxHeight())
        }
        AnimatedContent(
            targetState = targetState to variant,
            transitionSpec = OverlayTransitions.collection(),
            modifier = Modifier.width(1050.dp)
        ) { (targetState, variant) ->
            if (targetState != null) ItemDataCollection(variant(targetState), onItemSelect = onSelect)
            else Box(modifier = Modifier.requiredWidth(1050.dp).fillMaxHeight())
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ItemDataDetailOverlay(target: ItemData?) {
    OverlayBackdrop(target != null) { arctic.creation.target = null }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedContent(
            targetState = target,
            transitionSpec = OverlayTransitions.detail(),
            modifier = Modifier.fillMaxWidth().height(550.dp)
        ) { target ->
            if (target != null) ItemDataDetail(target)
            else Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ItemCreationVariantFilters(fixed: String?) {
    Column(horizontalAlignment = Alignment.End, modifier = Modifier.requiredWidth(160.dp).padding(top = 54.dp)) {
        ItemCreationVariantFilter("근거리", "Melee", fixed == null || fixed == "Melee")
        ItemCreationVariantFilter("원거리", "Ranged", fixed == null || fixed == "Ranged")
        ItemCreationVariantFilter("방어구", "Armor", fixed == null || fixed == "Armor")
        ItemCreationVariantFilter("유물", "Artifact", fixed == null || fixed == "Artifact")
    }
}

@Composable
private fun ItemCreationVariantFilter(text: String, variant: String, enabled: Boolean = true) {
    val selected = arctic.creation.filter == variant
    val onClick = { arctic.creation.filter = variant }

    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val blurAlpha by animateFloatAsState(if (!enabled) 0f else if (hovered) 0.8f else 0f)
    val overlayAlpha by animateFloatAsState(if (!enabled) 0.15f else if (selected) 1f else 0.6f)

    Box(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .hoverable(interaction, enabled = enabled)
            .clickable(interaction, null, onClick = onClick)
    ) {
        VariantFilterText(text, modifier = Modifier.blur(10.dp).alpha(blurAlpha))
        VariantFilterText(text, modifier = Modifier.alpha(overlayAlpha))
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
    val category = remember(variant) {
        when(variant) {
            "Melee" -> "근거리"
            "Ranged" -> "원거리"
            "Armor" -> "방어구"
            "Artifact" -> "유물"
            else -> throw RuntimeException("unknown variant to display data collection")
        }
    }
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
            ItemDataIconImage(data, modifier = Modifier.scale(1.1f).blur(10.dp).alpha(behindBlurAlpha))
            ItemDataIconImage(data)
        }
        Text(
            text = data.name ?: "알 수 없는 아이템",
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
fun ItemDataDetail(data: ItemData) {
    var rarity by remember { mutableStateOf(if (data.unique) "Unique" else "Common") }
    var power by remember { mutableStateOf(DungeonsPower.toSerializedPower(arctic.requireStored.playerPower.toDouble())) }

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
                    ItemRarityButton(
                        text = "${if (data.limited) "시즌한정 " else ""}${Localizations["/rarity_${rarity.lowercase()}"]}",
                        enabled = !data.unique,
                        color = RarityColor(rarity, RarityColorType.Translucent)
                    ) { rarity = if (rarity == "Common") "Rare" else "Common" }
                    Text(
                        text = data.name ?: "알 수 없는 아이템",
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
                if (data.variant != "Artifact") WarningText(text = "다른 옵션들은 추가한 뒤에 우측 영역에서 수정할 수 있어요!")
                if (data.variant == "Armor") WarningText(text = "추가 후 표시되는 기본 ArmorProperty 값은 수기로 기록된 것으로, 정확하지 않을 수 있습니다.")
            }
        }
        AddButton {
            val newItem = Item(
                parent = arctic.requireStored,
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

            newItem.parent.addItem(newItem)
            arctic.creation.disable()
            arctic.creation.target = null
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
            AddButtonIcon(modifier = Modifier.blur(10.dp).alpha(hoverAlpha))
            AddButtonIcon(modifier = Modifier.alpha(baseAlpha))
        }
        Text(
            text = "추가",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 25.dp)
                .alpha(baseAlpha)
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
