package arctic.ui.composables.inventory.collections

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import arctic.states.arctic
import arctic.ui.composables.atomic.RarityColor
import arctic.ui.composables.atomic.RarityColorType
import arctic.ui.composables.atomic.VariantFilterIcon
import arctic.ui.unit.dp
import arctic.ui.utils.rememberMutableInteractionSource
import dungeons.states.Item
import dungeons.states.extensions.data
import dungeons.states.extensions.totalEnchantmentInvestedPoints

@Composable
fun UnequippedItemCollection(items: List<Item>) {
    val variantFilter = remember { mutableStateOf<String?>(null) }
    val rarityFilter = remember { mutableStateOf<String?>(null) }

    val datasets by remember(items) {
        derivedStateOf {
            items.filter {
                val variantMatched =
                    (variantFilter.value == null || (if (variantFilter.value == "Enchanted") it.totalEnchantmentInvestedPoints > 0 else it.data.variant == variantFilter.value))
                val rarityMatched = (rarityFilter.value == null || it.rarity == rarityFilter.value)

                variantMatched && rarityMatched
            }
        }
    }

    Row {
        UnequippedItemFilterer(variantFilter, rarityFilter)
        ItemsLazyGrid(items = datasets) { _, item ->
            ItemGridItem(item)
        }
    }
}

@Composable
private fun UnequippedItemFilterer(
    variantFilter: MutableState<String?>,
    rarityFilter: MutableState<String?>
) {

    var variant by variantFilter
    var rarity by rarityFilter

    val variants = remember { listOf("Melee", "Armor", "Ranged", "Artifact", "Enchanted") }
    val rarities = remember { listOf("Unique", "Rare", "Common") }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(top = 15.dp)
    ) {
        for (item in variants) {
            VariantFilterButton(item, item == variant) { variant = item }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        for (item in rarities) {
            RarityFilterButton(item, item == rarity) { rarity = item }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        AddItemButton()
        Spacer(modifier = Modifier.height(20.dp))
    }

}

@Composable
private fun VariantFilterButton(variant: String, selected: Boolean, onClick: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val alpha = if (selected) 1f else if (hovered) 0.55f else 0.35f

    Image(
        bitmap = VariantFilterIcon(variant, selected || hovered),
        contentDescription = null,
        modifier = Modifier
            .size(60.dp)
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .padding(10.dp)
            .alpha(alpha)
    )
}

@Composable
private fun RarityFilterButton(rarity: String, selected: Boolean, onClick: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val alpha = if (selected) 1f else if (hovered) 0.55f else 0.35f

    Spacer(
        modifier = Modifier
            .size(60.dp)
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .rotate(-20f)
            .drawBehind {
                drawRoundRect(
                    RarityColor(rarity, RarityColorType.Opaque).copy(alpha = alpha),
                    topLeft = Offset(10.dp.value, size.height / 2 - 6.dp.value),
                    size = Size(size.width - 20.dp.value, 12.dp.value),
                    cornerRadius = CornerRadius(3.dp.value)
                )
            }
            .rotate(20f)
    )
}

@Composable
private fun AddItemButton() {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .size(60.dp)
            .hoverable(interaction)
            .clickable(interaction, null) { if(!arctic.alerts.checkAvailable()) arctic.creation.enable() }
            .padding(10.dp)
            .drawBehind {
                drawRect(
                    color = Color(if(hovered) 0xffffffff else 0xff79706b),
                    topLeft = Offset(size.width / 2 - 2.dp.value, 8.dp.value),
                    size = Size(4.dp.value, size.height - 16.dp.value)
                )
                drawRect(
                    Color(if(hovered) 0xffffffff else 0xff79706b),
                    topLeft = Offset(8.dp.value, size.height / 2 - 2.dp.value),
                    size = Size(size.width - 16.dp.value, 4.dp.value)
                )
            }
    )
}
