package composable.popup

import EnchantmentData
import Localizations
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composable.PopupCloseButton
import composable.inventory.BlurBehindImage
import editorState
import states.Enchantment
import states.Item

val EnchantmentSelectorDummy = EnchantmentData(id = "Unset", dataPath = "", multipleAllowed = true)

@Composable
fun EnchantmentSelectorView(item: Item, modifyTarget: Enchantment) {
    val available by remember {
        derivedStateOf {
            Database.current.enchantments
                .filter {
                    if (it.applyFor == null) return@filter false
                    if (!it.applyFor.contains(item.Type().name)) return@filter false

                    true
                }
                .sortedBy { Localizations.EnchantmentName(it) }
                .toMutableList()
                .apply { this.add(0, EnchantmentSelectorDummy) }
        }
    }

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = available.indexOfFirst { it.id == modifyTarget.id }.coerceAtLeast(0)
    )

    PopupCloseButton { editorState.detailState.toggleEnchantmentSelector() }

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
                onClick = { newEnchantmentData -> modifyTarget.changeId(newEnchantmentData.id) }
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
            text = Localizations.EnchantmentName(data),
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
fun BoxScope.EnchantmentIcon(enchantment: EnchantmentData, enabled: Boolean) =
    BlurBehindImage(
        bitmap = enchantment.Image(),
        alpha = if (enabled) 1f else 0.125f,
        enabled = enchantment.id != "Unset",
        modifier = Modifier.fillMaxSize().align(Alignment.Center).scale(enchantment.ImageScale() * 0.7f)
    )
