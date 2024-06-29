package kiwi.hoonkun.ui.composables.editor.collections

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.unit.TextUnit
import kiwi.hoonkun.ui.Resources
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.DungeonsJsonEditorState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.data
import minecraft.dungeons.values.DungeonsItem
import minecraft.dungeons.values.DungeonsPower
import java.util.*

@Composable
@NonSkippableComposable
fun <T>ItemsLazyGrid(
    columns: Int = 3,
    items: List<T>,
    itemContent: @Composable LazyGridItemScope.(T) -> Unit
) where T: MutableDungeons.Item? {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = items,
            key = { it?.internalId ?: UUID.randomUUID().toString() },
            itemContent = itemContent
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T>ItemGridItem(
    item: T,
    simplified: Boolean = false,
    selection: DungeonsJsonEditorState.SelectionState
) where T: MutableDungeons.Item? {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val modifier = Modifier
        .aspectRatio(1f / 1f)
        .hoverable(interaction)
        // 아래 .onClick 수정자가 특정 상황에서 클릭 시
        // ComposeSceneAccessible.getAccessibleChild 에서 IndexOutOfBoundsException 을 던진다.
        // 던지긴 하지만 다행히 앱이 죽지는 않는데, 추후 다른 것으로 변경할 수 있으면 할 것.
        .onClick(
            matcher = PointerMatcher.mouse(PointerButton.Primary),
            enabled = item != null,
            onClick = { if (item != null) selection.select(item, DungeonsJsonEditorState.SelectionState.Slot.Primary) }
        )
        .onClick(
            matcher = PointerMatcher.mouse(PointerButton.Secondary),
            enabled = item != null,
            onClick = { if (item != null) selection.select(item, DungeonsJsonEditorState.SelectionState.Slot.Secondary) }
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

    if (item == null) {
        EmptyItemSlot(modifier)
    } else {
        if (simplified)
            ItemSlotSimplified(item = item, modifier = modifier)
        else
            ItemSlot(item = item, modifier = modifier)
    }
}

@Composable
fun ItemSlot(
    item: MutableDungeons.Item,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 17.dp, vertical = 12.dp),
    fillFraction: Float = 0.8f,
    fontSize: TextUnit = 22.sp
) {
    Box(modifier = modifier) {
        ItemImage(item = item, fillFraction = fillFraction)

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(contentPadding)
        ) {
            PowerText(
                power = item.power,
                fontSize = fontSize,
                modifier = Modifier.align(Alignment.BottomEnd)
            )

            if (item.enchanted) {
                InvestedEnchantmentPointsText(
                    points = item.totalEnchantmentInvestedPoints,
                    fontSize = fontSize,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }

        if (item.markedNew == true) {
            NewMark(modifier = Modifier.align(Alignment.TopStart))
        }
    }
}

@Composable
fun ItemSlotSimplified(
    item: MutableDungeons.Item,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        ItemImage(item = item)
    }
}

@Composable
private fun EmptyItemSlot(
    modifier: Modifier
) =
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        RarityColor(DungeonsItem.Rarity.Common, RarityColorType.Translucent),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 7.dp,
                brush = Brush.linearGradient(
                    listOf(
                        RarityColor(DungeonsItem.Rarity.Common, RarityColorType.Opaque),
                        Color.Transparent,
                        RarityColor(DungeonsItem.Rarity.Common, RarityColorType.Opaque)
                    )
                ),
                shape = RectangleShape
            )
            .padding(20.dp)
            .then(modifier)
    )

@Composable
private fun PowerText(
    power: Double,
    modifier: Modifier,
    fontSize: TextUnit = 22.sp
) =
    Text(
        text = "${remember(power) { DungeonsPower.toInGamePower(power).toInt()} }",
        style = LocalTextStyle.current.copy(
            color = Color.White.copy(alpha = 0.85f),
            fontSize = fontSize,
            fontFamily = Resources.Fonts.JetbrainsMono,
            shadow = Shadow(Color.Black, blurRadius = 5f)
        ),
        modifier = modifier
    )

@Composable
private fun InvestedEnchantmentPointsText(
    points: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp
) =
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(IntrinsicSize.Min).then(modifier)
    ) {
        Text(
            text = "$points",
            style = LocalTextStyle.current.copy(
                color = Color.White.copy(alpha = 0.85f),
                fontSize = fontSize,
                fontFamily = Resources.Fonts.JetbrainsMono,
                shadow = Shadow(Color.Black, blurRadius = 5f)
            )
        )
        Spacer(modifier = Modifier.width(5.dp))
        Image(
            bitmap = DungeonsTextures["/Game/UI/Materials/Inventory2/Item/salvage_enchanticon.png"],
            contentDescription = null,
            modifier = Modifier
                .applyZeroIntrinsics()
                .fillMaxHeight(0.6f)
                .aspectRatio(1f / 1f)
        )
    }

@Composable
private fun ItemImage(
    item: MutableDungeons.Item,
    fillFraction: Float = 0.8f,
) =
    Image(
        bitmap = item.data.inventoryIcon,
        contentDescription = null,
        alignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawItemFrame(item.rarity, item.glided, item.enchanted, item.data.variant == DungeonsItem.Variant.Artifact)
                scale(fillFraction) {
                    this@drawWithContent.drawContent()
                }
            }
    )

@Composable
private fun NewMark(
    modifier: Modifier = Modifier
) =
    Image(
        bitmap = DungeonsTextures["/Game/UI/Materials/HotBar2/Icons/inventoryslot_newitem.png"],
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize(0.2f)
            .scale(scaleX = -1f, scaleY = 1f)
            .then(modifier)
    )
