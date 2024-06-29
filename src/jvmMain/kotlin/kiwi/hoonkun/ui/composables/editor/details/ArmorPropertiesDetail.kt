package kiwi.hoonkun.ui.composables.editor.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kiwi.hoonkun.ui.composables.base.rememberArmorPropertyIcon
import kiwi.hoonkun.ui.composables.overlays.ArmorPropertyOverlay
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.skeleton
import minecraft.dungeons.values.DungeonsArmorProperty

@Composable
fun ItemArmorProperties(item: MutableDungeons.Item, properties: List<MutableDungeons.ArmorProperty>) {
    val overlays = LocalOverlayState.current

    val groupedProperties by remember(properties) {
        derivedStateOf {
            val sorted = properties.sortedBy { it.skeleton.description?.length }
            val uniques = sorted.filter { it.rarity == DungeonsArmorProperty.Rarity.Unique }
            val commons = sorted.filter { it.rarity == DungeonsArmorProperty.Rarity.Common }

            uniques.groupByLength() + commons.groupByLength()
        }
    }

    val height = remember(groupedProperties) { groupedProperties.height() }

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            userScrollEnabled = false,
            modifier = Modifier
                .weight(1f)
                .requiredHeight(height)
        ) {
            items(
                items = groupedProperties,
                span = { GridItemSpan(it.second) }
            ) { (property) ->
                ArmorPropertyItem(property) {
                    overlays.make(
                        enter = defaultFadeIn(),
                        exit = defaultFadeOut()
                    ) {
                        ArmorPropertyOverlay(
                            holder = item,
                            initialSelected = property,
                            requestClose = it
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        ArmorPropertyAddButton(item = item)
    }
}

@Composable
fun ArmorPropertyItem(
    property: MutableDungeons.ArmorProperty,
    selected: () -> Boolean = { false },
    onClick: () -> Unit
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val propertyRarityIcon = rememberArmorPropertyIcon(property)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .requiredHeight(ItemHeight)
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
                if (selected()) drawIndicator(0.2f)
            }
            .hoverable(interaction)
            .clickable(interaction, null) { onClick() }
    ) {
        Image(
            bitmap = propertyRarityIcon,
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = property.skeleton.description ?: property.id,
            fontSize = 25.sp,
            color = Color.White
        )
    }
}

@Composable
private fun ArmorPropertyAddButton(item: MutableDungeons.Item) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val overlays = LocalOverlayState.current

    Spacer(
        modifier = Modifier
            .size(35.dp)
            .drawBehind {
                if (hovered) {
                    drawRoundRect(
                        color = Color.White,
                        alpha = 0.15f,
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                }
                val draw: DrawScope.() -> Unit = {
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(size.width / 2 - 2.dp.toPx(), 8.dp.toPx()),
                        size = Size(4.dp.toPx(), size.height - 16.dp.toPx())
                    )
                }
                draw()
                rotate(degrees = 90f, block = draw)
            }
            .hoverable(interaction)
            .clickable(interaction, null) {
                overlays.make(
                    enter = defaultFadeIn(),
                    exit = defaultFadeOut()
                ) {
                    ArmorPropertyOverlay(
                        holder = item,
                        initialSelected = null,
                        requestClose = it
                    )
                }
            }
    )
}

private val ItemHeight get() = 40.dp

fun List<MutableDungeons.ArmorProperty>.groupByLength(): List<Pair<MutableDungeons.ArmorProperty, Int>> {
    val result = mutableListOf<Pair<MutableDungeons.ArmorProperty, Int>>()
    forEach {
        val description = it.skeleton.description ?: it.skeleton.id
        val long = description.fold(0) { acc, c -> acc + if (c == ' ') 1 else 3 } > 35
        result.add(it to if (long) 2 else 1)
    }
    return result
}

fun List<Pair<MutableDungeons.ArmorProperty, Int>>.height(): Dp {
    var row = 1; var column = 0
    forEach { (_, span) ->
        if (column + span > 2) {
            row++
            column = span
        } else {
            column += span
        }
    }
    return ItemHeight * row
}
