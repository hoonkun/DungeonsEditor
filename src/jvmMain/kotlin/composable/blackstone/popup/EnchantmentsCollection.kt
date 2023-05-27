package composable.blackstone.popup

import Database
import EnchantmentData
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
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
import arctic
import blackstone.states.Enchantment
import blackstone.states.Item
import blackstone.states.items.*
import composable.inventory.BlurBehindImage
import extensions.replace

@Composable
fun EnchantmentsCollection(holder: Item, index: Int, isNetheriteEnchant: Boolean) {

    val target = (if (isNetheriteEnchant) holder.netheriteEnchant else holder.enchantments!![index])
        ?: throw RuntimeException("[EnchantmentCollection] non-null assertion failed: holder.netheriteEnchant must not be null")

    val enchantments by remember(holder.data.variant) {
        derivedStateOf {
            Database.current.enchantments.filter { it.applyFor?.contains(holder.data.variant) == true }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .requiredWidth(700.dp)
            .fillMaxHeight()
            .background(Color(0xff080808)),
        contentPadding = PaddingValues(vertical = 60.dp, horizontal = 10.dp)
    ) {
        items(enchantments, key = { it.id }) { enchantment ->
            EnchantmentSelectButton(
                data = enchantment,
                enabled = enchantment.multipleAllowed || (holder.enchantments?.all { it.id != enchantment.id } ?: true),
                selected = target.id == enchantment.id,
                onClick = { newEnchantmentData ->
                    val newId = if (newEnchantmentData.id == target.id) "Unset" else newEnchantmentData.id

                    val newEnchantment = Enchantment(newId, holder, isNetheriteEnchant = target.isNetheriteEnchant)
                    newEnchantment.leveling(
                        if (newId != "Unset") target.level else 0,
                        isNetheriteEnchant = target.isNetheriteEnchant
                    )

                    if (target.isNetheriteEnchant) {
                        holder.netheriteEnchant = newEnchantment
                    } else {
                        holder.enchantments?.replace(target, newEnchantment)
                    }

                    holder.recalculateEnchantmentPoints()
                    arctic.enchantments.viewDetail(newEnchantment)
                }
            )
        }
    }
}

@Composable
fun EnchantmentSelectButton(data: EnchantmentData, enabled: Boolean, selected: Boolean, onClick: (EnchantmentData) -> Unit) {
    Debugging.recomposition("EnchantmentSelectButton")

    val source = remember { MutableInteractionSource() }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / 1f)
                .clickable(source, null, enabled = enabled || selected) { onClick(data) }
        ) {
            EnchantmentIcon(data, enabled, selected)
        }
        Text(
            text = data.name,
            style = TextStyle(
                color = if (enabled || selected) Color.White else Color.White.copy(alpha = 0.5f),
                fontSize = 19.sp,
                fontWeight = if (enabled || selected) FontWeight.Bold else FontWeight.Normal
            ),
            modifier = Modifier.offset(y = (-12).dp)
        )
    }
}

@Composable
fun BoxScope.EnchantmentIcon(enchantment: EnchantmentData, enabled: Boolean, selected: Boolean) {
    Debugging.recomposition("EnchantmentIcon")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

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
                if (!enabled && !selected) return@drawBehind
                drawRect(
                    Color.White,
                    style = Stroke(width = 6.dp.value),
                    size = Size(size.width * 0.6f, size.height * 0.6f),
                    alpha = if (selected) 0.8f else if (hovered) 0.3f else 0.0f,
                    topLeft = Offset(size.width * 0.2f, size.height * 0.2f)
                )
            }
            .rotate(-45f)
    )
}
