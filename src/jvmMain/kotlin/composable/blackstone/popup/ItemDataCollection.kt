package composable.blackstone.popup

import dungeons.Database
import dungeons.ItemData
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import arctic.ui.composables.RarityColor
import arctic.ui.composables.RarityColorType
import extensions.padEnd


private fun variant(type: String): (ItemData) -> Boolean = { it.variant == type }
private val nameType: (ItemData) -> String = { "${if (it.unique) 0 else 1}${it.type.replace(Regex("_.+"), "")}_${it.name}" }

private fun <T> List<T>.padEndRemaining(multiplier: Int, factory: (Int) -> T) = this.toMutableList().padEnd(size + (multiplier - size % multiplier), factory)

private const val Columns = 5

private fun variants(variant: String) = Database.items.filter(variant(variant)).sortedBy(nameType).padEndRemaining(Columns) { null }

private val VariantText = mapOf(
    "Melee" to "근거리",
    "Ranged" to "원거리",
    "Armor" to "방어구",
    "Artifact" to "유물"
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemDataCollection(variant: String, onItemSelect: (ItemData) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(Columns),
        contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
        modifier = Modifier
            .fillMaxHeight()
            .requiredWidth(950.dp)
            .background(Color(0xff080808))
            .onClick {  }
            .padding(horizontal = 20.dp),
    ) {
        item(span = { GridItemSpan(5) }) { CategoryText(VariantText[variant]!!) }
        items(variants(variant)) {
            ItemDataView(it, onItemSelect)
        }
    }

}

@Composable
fun CategoryText(text: String) =
    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.height(115.dp).padding(start = 20.dp, bottom = 15.dp)) {
        Text(text = text, color = Color.White, fontSize = 35.sp, fontWeight = FontWeight.Bold)
    }

@Composable
fun CategoryDivider() =
    Spacer(modifier = Modifier.height(43.dp).padding(horizontal = 200.dp).padding(top = 40.dp).background(Color.White.copy(0.2f)))

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemDataView(item: ItemData?, onItemSelect: (ItemData) -> Unit) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    val alpha by animateFloatAsState(if (hovered) 1f else 0f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .hoverable(source)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) { if (item != null) onItemSelect(item) }
    ) {
        if (item != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f / 1f)
                    .padding(20.dp)
                    .rotate(-20f)
                    .drawBehind {
                        val color = RarityColor("Unique", RarityColorType.Opaque)
                        if (item.unique) {
                            drawRect(
                                Brush.linearGradient(
                                    0f to color.copy(alpha = 0f),
                                    0.5f to color.copy(alpha = 0.75f),
                                    1f to color.copy(alpha = 0f),
                                    start = Offset(0f, size.height / 2f),
                                    end = Offset(size.width, size.height / 2f)
                                ),
                                topLeft = Offset(0f, size.height / 2 - 3.dp.value),
                                size = Size(size.width, 6.dp.value)
                            )
                            drawCircle(
                                Brush.radialGradient(
                                    0f to color.copy(alpha = 0.45f),
                                    1f to color.copy(alpha = 0f),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                )
                            )
                        }
                    }
                    .rotate(20f)
            ) {
                Image(item.inventoryIcon, null, modifier = Modifier.fillMaxSize().scale(1.1f).blur(10.dp).alpha(alpha))
                Image(item.inventoryIcon, null, modifier = Modifier.fillMaxSize())
            }
            Text(text = item.name ?: "알 수 없는 아이템", color = Color.White, fontSize = 16.sp, modifier = Modifier.offset(y = (-10).dp))
        }
    }
}
