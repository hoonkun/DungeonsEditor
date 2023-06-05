package arctic.ui.composables.inventory.collections

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import arctic.states.arctic
import arctic.ui.composables.atomic.RarityColor
import arctic.ui.composables.atomic.RarityColorType
import arctic.ui.composables.atomic.drawItemFrame
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import arctic.ui.utils.rememberMutableInteractionSource
import dungeons.DungeonsPower
import dungeons.IngameImages
import dungeons.states.Item
import dungeons.states.extensions.data
import dungeons.states.extensions.totalEnchantmentInvestedPoints

@Composable
fun <T>ItemsLazyGrid(columns: Int = 3, items: List<T>, content: @Composable LazyGridItemScope.(Int, T) -> Unit) where T: Item? =
    LazyVerticalGrid(
        GridCells.Fixed(columns),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) { itemsIndexed(items, itemContent = content) }


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T>ItemGridItem(item: T, simplified: Boolean = false) where T: Item? {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .aspectRatio(1f / 1f)
            .padding(5.dp)
            .hoverable(interaction)
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Primary),
                enabled = item != null,
                onClick = { if (item != null) arctic.selection.select(item, 0) }
            )
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Secondary),
                enabled = item != null,
                onClick = { if (item != null) arctic.selection.select(item, 1) }
            )
            .drawBehind {
                val brush =
                    if (arctic.selection.selected(item))
                        Brush.linearGradient(listOf(Color(0xeeffffff), Color(0xaaffffff), Color(0xeeffffff)))
                    else if (hovered)
                        Brush.linearGradient(listOf(Color(0x75ffffff), Color(0x25ffffff), Color(0x75ffffff)))
                    else
                        return@drawBehind

                drawRect(
                    brush = brush,
                    topLeft = Offset(-10.dp.value, -10.dp.value),
                    size = Size(size.width + 20.dp.value, size.height + 20.dp.value),
                    style = Stroke(width = 4.dp.value)
                )
            }
    ) {
        if (item == null) {
            EmptyEquippedSlot()
        } else {
            val totalEnchantPoints = item.totalEnchantmentInvestedPoints

            Image(
                bitmap = item.data.inventoryIcon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent { drawItemFrame(item.rarity, item.netheriteEnchant != null, item.totalEnchantmentInvestedPoints > 0) }
                    .padding(if (simplified) 12.5.dp else 20.dp)
            )

            if (!simplified) {
                Text(
                    text = "${DungeonsPower.toInGamePower(item.power).toInt()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(vertical = 8.dp, horizontal = 13.dp)
                )

                if (totalEnchantPoints != 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.TopEnd).padding(vertical = 8.dp, horizontal = 13.dp)
                    ) {
                        Image(
                            bitmap = IngameImages.get { "/Game/UI/Materials/Inventory2/Item/salvage_enchanticon.png" },
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$totalEnchantPoints",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            modifier = Modifier.offset(y = (-2).dp)
                        )
                    }
                }

                if (item.markedNew == true) {
                    Image(
                        bitmap = IngameImages.get { "/Game/UI/Materials/HotBar2/Icons/inventoryslot_newitem.png" },
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxSize(0.2f)
                            .offset(2.dp, (-1.5).dp)
                            .scale(scaleX = -1f, scaleY = 1f)
                    )
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
