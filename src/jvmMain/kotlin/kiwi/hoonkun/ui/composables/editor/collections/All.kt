package kiwi.hoonkun.ui.composables.editor.collections

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.TextStyle
import kiwi.hoonkun.ui.Resources
import kiwi.hoonkun.ui.reusables.RarityColor
import kiwi.hoonkun.ui.reusables.RarityColorType
import kiwi.hoonkun.ui.reusables.drawItemFrame
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.values.DungeonsPower

@Composable
fun <T>ItemsLazyGrid(
    columns: Int = 3,
    items: List<T>,
    itemContent: @Composable LazyGridItemScope.(T) -> Unit
) where T: Item? =
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items = items, itemContent = itemContent)
    }


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T>ItemGridItem(item: T, simplified: Boolean = false, selection: EditorState.SelectionState) where T: Item? {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .aspectRatio(1f / 1f)
            .hoverable(interaction)
            // 아래 .onClick 수정자가 특정 상황에서 클릭 시
            // ComposeSceneAccessible.getAccessibleChild 에서 IndexOutOfBoundsException 을 던진다.
            // 던지긴 하지만 다행히 앱이 죽지는 않는데, 추후 다른 것으로 변경할 수 있으면 할 것.
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Primary),
                enabled = item != null,
                onClick = { if (item != null) selection.select(item, EditorState.SelectionState.Slot.Primary) }
            )
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Secondary),
                enabled = item != null,
                onClick = { if (item != null) selection.select(item, EditorState.SelectionState.Slot.Secondary) }
            )
            .drawBehind {
                val brush =
                    if (selection.selected(item))
                        Brush.linearGradient(listOf(Color(0xeeffffff), Color(0xaaffffff), Color(0xeeffffff)))
                    else if (hovered)
                        Brush.linearGradient(listOf(Color(0x75ffffff), Color(0x25ffffff), Color(0x75ffffff)))
                    else
                        return@drawBehind

                drawRect(
                    brush = brush,
                    topLeft = Offset(-5.dp.toPx(), -5.dp.toPx()),
                    size = Size(size.width + 10.dp.toPx(), size.height + 10.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
    ) {
        if (item == null) {
            EmptyEquippedSlot()
        } else {
            ItemImage(item, simplified)

            if (!simplified) {
                PowerText(item.power)

                if (item.enchanted)
                    InvestedEnchantmentPointsText(item.totalEnchantmentInvestedPoints)

                if (item.markedNew == true) {
                    NewMark()
                }
            }
        }
    }
}

@Composable
private fun EmptyEquippedSlot() =
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(RarityColor("Common", RarityColorType.Translucent), Color.Transparent)))
            .border(7.dp, Brush.linearGradient(listOf(RarityColor("Common", RarityColorType.Opaque), Color.Transparent, RarityColor("Common", RarityColorType.Opaque))), shape = RectangleShape)
            .padding(20.dp)
    )

@Composable
private fun BoxScope.PowerText(power: Double) =
    Text(
        text = "${remember(power) { DungeonsPower.toInGamePower(power).toInt()} }",
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 22.sp,
        fontFamily = Resources.Fonts.JetbrainsMono,
        style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 5f)),
        modifier = Modifier.align(Alignment.BottomEnd).padding(horizontal = 12.dp, vertical = 9.dp)
    )

@Composable
private fun BoxScope.InvestedEnchantmentPointsText(points: Int) =
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.align(Alignment.TopEnd).padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            text = "$points",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 22.sp,
            fontFamily = Resources.Fonts.JetbrainsMono,
            style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 5f))
        )
        Spacer(modifier = Modifier.width(5.dp))
        Image(
            bitmap = DungeonsTextures["/Game/UI/Materials/Inventory2/Item/salvage_enchanticon.png"],
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }

@Composable
private fun ItemImage(item: Item, simplified: Boolean) =
    Image(
        bitmap = item.data.inventoryIcon,
        contentDescription = null,
        alignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawItemFrame(item.rarity, item.glided, item.enchanted, item.data.variant == "Artifact") }
            .padding(all = if (simplified) 12.5.dp else 20.dp)
    )

@Composable
private fun BoxScope.NewMark() =
    Image(
        bitmap = DungeonsTextures["/Game/UI/Materials/HotBar2/Icons/inventoryslot_newitem.png"],
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.TopStart)
            .fillMaxSize(0.2f)
            .offset(2.dp, (-1.5).dp)
            .scale(scaleX = -1f, scaleY = 1f)
    )
