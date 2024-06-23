package kiwi.hoonkun.ui.composables.overlays

import LocalWindowState
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import dungeons.DungeonsPower
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.*
import kiwi.hoonkun.ui.composables.editor.collections.ItemSlot
import kiwi.hoonkun.ui.composables.editor.details.ArmorPropertyItem
import kiwi.hoonkun.ui.composables.editor.details.groupByLength
import kiwi.hoonkun.ui.composables.editor.details.height
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.ArmorProperty
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import kiwi.hoonkun.utils.toFixed
import minecraft.dungeons.resources.ArmorPropertyData
import minecraft.dungeons.resources.DungeonsDatabase
import minecraft.dungeons.resources.DungeonsLocalizations


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedVisibilityScope.ArmorPropertyOverlay(
    holder: Item,
    initialSelected: ArmorProperty?,
    requestClose: () -> Unit
) {
    val density = LocalDensity.current
    val windowState = LocalWindowState.current

    val parentHeight = (windowState.size.height - DetailHeight) / 2f
    val childOffset = parentHeight / 2f - DetailHeight / 2f - 25.dp

    val state = rememberArmorPropertyOverlayState(holder, initialSelected)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        ArmorPropertyDataCollection(
            state = state,
            modifier = Modifier.animateEnterExit(
                enter = slideIn { IntOffset(-60.dp.value.toInt(), 0) },
                exit = ExitTransition.None
            )
        )
        Spacer(modifier = Modifier.width(40.dp))
        Box(modifier = Modifier.requiredSize(675.dp, parentHeight)) {
            HolderPreview(
                state = state,
                offset = childOffset,
                modifier = Modifier
                    .animateEnterExit(
                        enter = slideIn { with(density) { IntOffset(-60.dp.roundToPx(), 0) } },
                        exit = ExitTransition.None
                    )
                    .animateContentSize()
            )
            AnimatedContent(
                targetState = state.selected,
                transitionSpec = {
                    val initialIndex = state.properties.indexOf(initialState)
                    val targetIndex = state.properties.indexOf(targetState)

                    val offset = with(density) {
                        if (initialIndex < targetIndex) 50.dp.roundToPx()
                        else (-50).dp.roundToPx()
                    }

                    val enter = defaultFadeIn() + slideIn { IntOffset(offset, 0) }
                    val exit = defaultFadeOut() + slideOut { IntOffset(-offset, 0) }

                    enter togetherWith exit using SizeTransform(clip = false)
                },
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .animateEnterExit(
                        enter = slideIn { with(density) { IntOffset(0, 60.dp.roundToPx()) } },
                        exit = ExitTransition.None
                    )
            ) { selected ->
                ArmorPropertyDetail(state, selected)
                Row(modifier = Modifier.offsetRelative(x = 0f, y = 1f).offset(y = -childOffset)) {
                    Spacer(modifier = Modifier.weight(1f))
                    RetroButton(
                        text = Localizations.UiText("cancel"),
                        color = Color.White,
                        hoverInteraction = RetroButtonHoverInteraction.Overlay,
                        onClick = { requestClose() },
                        modifier = Modifier.size(125.dp, 55.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    RetroButton(
                        text = Localizations.UiText("ok"),
                        color = Color(0xff3f8e4f),
                        hoverInteraction = RetroButtonHoverInteraction.Outline,
                        onClick = {
                            state.holder.armorProperties = state.properties
                            requestClose()
                        },
                        modifier = Modifier.size(125.dp, 55.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HolderPreview(
    state: ArmorPropertyOverlayState,
    offset: Dp,
    modifier: Modifier = Modifier,
) {
    val holder = state.holder

    val groupedProperties by remember(state.properties) {
        derivedStateOf {
            val sorted = state.properties.sortedBy { it.data.description?.length }
            val uniques = sorted.filter { it.rarity.lowercase() == "unique" }
            val commons = sorted.filter { it.rarity.lowercase() == "common" }

            uniques.groupByLength() + commons.groupByLength()
        }
    }

    val height = remember(groupedProperties) { groupedProperties.height() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .offset(y = offset)
            .offsetRelative(x = 0f, y = -1f)
            .then(modifier)
            .background(Color(0xff080808))
            .clipToBounds()
            .drawBehind {
                drawImage(
                    image = holder.data.largeIcon,
                    dstOffset = Offset((-20f).dp.toPx(), -30f.dp.toPx()).round(),
                    dstSize = Size(size.width * 0.5f, size.width * 0.5f).round(),
                    alpha = 0.25f
                )
            }
            .consumeClick()
            .padding(20.dp)
    ) {
        Row {
            ItemSlot(
                item = holder,
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(1f / 1f)
                    .padding(all = 12.dp)
            )
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp).padding(top = 12.dp, bottom = 8.dp)
                ) {
                    ItemRarityButton(data = holder.data, rarity = holder.rarity, readonly = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    ItemNetheriteEnchantButton(
                        holder = holder,
                        enchantment = holder.netheriteEnchant,
                        enabled = false,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ItemModifiedButton(holder = holder, readonly = true)
                }
                Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(
                        text = holder.data.name, fontSize = 40.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp).alignByBaseline()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alignByBaseline()) {
                        PowerIcon()
                        Text(
                            text = "${DungeonsLocalizations["/gearpower_POWER"]} ${DungeonsPower.toInGamePower(holder.power).toFixed(3)}",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            userScrollEnabled = false,
            contentPadding = PaddingValues(all = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(height + AddButtonHeight + 24.dp)
        ) {
            items(
                items = groupedProperties,
                span = { GridItemSpan(it.second) }
            ) { (property) ->
                ArmorPropertyItem(
                    property = property,
                    selected = { state.selected === property },
                    onClick = { state.selected = property }
                )
            }
            item(span = { GridItemSpan(2) }) {
                AddButton(state = state)
            }
        }
    }
}

@Composable
private fun ArmorPropertyDataCollection(state: ArmorPropertyOverlayState, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.initialIndex,
        initialFirstVisibleItemScrollOffset = -602.dp.value.toInt()
    )

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 30.dp),
        modifier = Modifier
            .requiredWidth(700.dp)
            .fillMaxHeight()
            .then(modifier)
            .background(Color(0xff080808))
    ) {
        items(state.datasets, key = { it.id }) { data ->
            ArmorPropertyCollectionItem(data = data, state = state)
        }
    }
}

@Composable
private fun ArmorPropertyCollectionItem(
    data: ArmorPropertyData,
    state: ArmorPropertyOverlayState
) {
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val style = LocalTextStyle.current

    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val selected = state.selected.let { it != null && it.id == data.id }

    val onItemClick: () -> Unit = {
        val holder = state.holder
        val replaceFrom = state.selected
        val properties = state.properties

        val newProperty = ArmorProperty(holder, data.id)

        if (replaceFrom != null) {
            if (replaceFrom.id == data.id) {
                properties.remove(replaceFrom)
                state.selected = null
            } else {
                replaceFrom.id = newProperty.id
            }
        } else {
            properties.add(newProperty)
            state.selected = newProperty
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onItemClick)
            .drawBehind {
                drawRect(
                    Color.White.copy(
                        alpha =
                            if (selected) 0.3f
                            else if (hovered) 0.15f
                            else 0f
                    )
                )

                if (!selected) return@drawBehind

                val textOffset = 16.dp.toPx()
                val center = Offset(size.width - size.height / 2f - 4.dp.toPx(), center.y - textOffset)
                val textLayoutResult = TextMeasurer(
                    fontFamilyResolver,
                    this,
                    LayoutDirection.Ltr
                )
                    .measure(
                        text = Localizations.UiText("effect_delete_multiline"),
                        style = style.copy(fontSize = 12.sp, textAlign = TextAlign.Center)
                    )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = center + Offset(x = -textLayoutResult.size.width / 2f, y = textOffset)
                )

                rotate(45f, center) {
                    val diff = 12.dp.toPx()
                    val strokeWidth = 2.dp.toPx()
                    val yOffset = Offset(x = 0f, y = diff)
                    val xOffset = Offset(x = diff, y = 0f)
                    drawLine(
                        color = Color.White,
                        start = center - yOffset,
                        end = center + yOffset,
                        strokeWidth = strokeWidth,
                    )
                    drawLine(
                        color = Color.White,
                        start = center - xOffset,
                        end = center + xOffset,
                        strokeWidth = strokeWidth,
                    )
                }
            }
            .padding(vertical = 10.dp, horizontal = 30.dp)
    ) {
        Text(text = data.description!!, fontSize = 26.sp)
        Text(text = data.id, fontSize = 18.sp, modifier = Modifier.alpha(0.5f))
    }
}

private val DetailHeight = 140.dp

@Composable
private fun ArmorPropertyDetail(
    state: ArmorPropertyOverlayState,
    property: ArmorProperty?
) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .requiredSize(675.dp, DetailHeight)
            .background(Color(0xff080808))
            .consumeClick()
            .padding(30.dp)
    ) {
        AnimatedContent(
            targetState = property?.data,
            transitionSpec = {
                val initialIndex = state.datasets.indexOf(initialState)
                val targetIndex = state.datasets.indexOf(targetState)

                val offset = with(density) {
                    if (initialIndex < targetIndex) 50.dp.roundToPx()
                    else (-50).dp.roundToPx()
                }

                val enter = defaultFadeIn() + slideIn { IntOffset(0, offset) }
                val exit = defaultFadeOut() + slideOut { IntOffset(0, -offset) }

                enter togetherWith exit
            },
            contentAlignment = Alignment.CenterStart,
        ) { capturedPropertyData ->
            if (capturedPropertyData != null) {
                Column {
                    Text(text = Localizations.UiText("armor_property"), fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IfNotNull(property) { ArmorPropertyRarityToggle(it) }
                        Spacer(modifier = Modifier.width(15.dp))
                        AutosizeText(
                            text = capturedPropertyData.description!!,
                            maxFontSize = 32.sp,
                            contentAlignment = Alignment.CenterStart,
                            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = Localizations.UiText("armor_property_empty"))
                }
            }
        }
    }
}

@Composable
private fun ArmorPropertyRarityToggle(property: ArmorProperty) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val rarityIcon = rememberArmorPropertyIconAsState(property)

    Image(
        bitmap = rarityIcon,
        contentDescription = null,
        modifier = Modifier
            .size(41.dp)
            .offset(y = 1.5.dp)
            .hoverable(interaction)
            .clickable(interaction, null) { property.rarity = if (property.rarity == "Common") "Unique" else "Common" }
            .background(Color.White.copy(if (hovered) 0.3f else 0f), shape = RoundedCornerShape(6.dp.value))
            .padding(3.dp)
    )
}

val AddButtonHeight = 40.dp

@Composable
private fun AddButton(state: ArmorPropertyOverlayState) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(AddButtonHeight)
            .drawBehind {
                val drawIndicator: (Float) -> Unit = { alpha ->
                    drawRoundRect(
                        color = Color.White.copy(alpha = alpha),
                        topLeft = Offset(-10.dp.toPx(), 0f),
                        size = Size(size.width + 20.dp.toPx(), size.height),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                }
                drawIndicator(if (hovered) 0.1f else 0.0f)
                if (state.selected == null) drawIndicator(0.2f)

                val diff = 10.dp.toPx()
                val strokeWidth = 3.dp.toPx()
                val yOffset = Offset(x = 0f, y = diff)
                val xOffset = Offset(x = diff, y = 0f)
                drawLine(
                    color = Color.White,
                    start = center - yOffset,
                    end = center + yOffset,
                    strokeWidth = strokeWidth,
                )
                drawLine(
                    color = Color.White,
                    start = center - xOffset,
                    end = center + xOffset,
                    strokeWidth = strokeWidth,
                )
            }
            .hoverable(interaction)
            .clickable(interaction, null) { state.selected = null }
    )
}

@Stable
private class ArmorPropertyOverlayState(
    val holder: Item,
    initialSelected: ArmorProperty?,
) {
    val datasets = DungeonsDatabase.armorProperties
        .filter { it.description != null }
        .sortedBy { it.id }
    val initialIndex = datasets
        .indexOf(initialSelected?.data)
        .coerceAtLeast(0)

    val properties = holder.armorProperties?.map { it.copy() }?.toMutableStateList() ?: mutableStateListOf()
    var selected by mutableStateOf(properties.getOrNull(holder.armorProperties?.indexOf(initialSelected) ?: -1))
}

@Composable
private fun rememberArmorPropertyOverlayState(
    holder: Item,
    initialSelected: ArmorProperty?
) =
    remember(holder, initialSelected) {
        ArmorPropertyOverlayState(holder, initialSelected)
    }
