package composable.popup

import EnchantmentData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composable.inventory.BlurBehindImage
import editorState
import blackstone.states.Enchantment
import blackstone.states.Item
import blackstone.states.items.changeInto
import blackstone.states.items.data

val EnchantmentSelectorDummy = EnchantmentData(id = "Unset", dataPath = "", multipleAllowed = true)

@Composable
fun EnchantmentSelectorView(item: Item, modifyTarget: Enchantment) {
    val available by remember {
        derivedStateOf {
            Database.current.enchantments
                .filter {
                    if (it.applyFor == null) return@filter false
                    if (!it.applyFor.contains(item.data.variant)) return@filter false

                    true
                }
                .sortedBy { it.name }
                .toMutableList()
                .apply { this.add(0, EnchantmentSelectorDummy) }
        }
    }

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = available.indexOfFirst { it.id == modifyTarget.id }.coerceAtLeast(0)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(20.dp),
        state = gridState,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(available) {
            EnchantmentSelectButton(
                data = it,
                enabled = it.multipleAllowed || item.enchantments?.any { appliedEnchantment -> appliedEnchantment.id == it.id } != true,
                onClick = { newEnchantmentData -> modifyTarget.changeInto(newEnchantmentData.id) }
            )
        }
    }
}

@Composable
fun EnchantmentSelectButton(data: EnchantmentData, enabled: Boolean, onClick: (EnchantmentData) -> Unit) {
    val source = remember { MutableInteractionSource() }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / 1f)
                .clickable(source, null, enabled = enabled) { onClick(data) }
        ) {
            EnchantmentIcon(data, enabled)
        }
        Text(
            text = data.name,
            style = TextStyle(
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                fontSize = 16.sp,
                fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal
            ),
            modifier = Modifier.offset(y = (-10).dp)
        )
    }
}

@Composable
fun BoxScope.EnchantmentIcon(enchantment: EnchantmentData, enabled: Boolean) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    val selected = enchantment.id == editorState.detail.selectedEnchantment?.id

    BlurBehindImage(
        bitmap = enchantment.icon,
        alpha = if (enabled || selected) 1f else 0.125f,
        enabled = enchantment.id != "Unset",
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center)
            .scale(enchantment.iconScale * 0.7f)
            .hoverable(source)
            .rotate(45f)
            .drawBehind {
                drawRect(
                    if (selected) Color.White.copy(alpha = 0.4f) else if (hovered) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                    style = Stroke(width = 6.dp.value),
                    size = Size(size.width * 0.6f, size.height * 0.6f),
                    topLeft = Offset(size.width * 0.2f, size.height * 0.2f)
                )
            }
            .rotate(-45f)
    )
}
