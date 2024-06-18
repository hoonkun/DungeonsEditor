package kiwi.hoonkun.ui.composables.editor.collections

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kiwi.hoonkun.ui.reusables.RarityColor
import kiwi.hoonkun.ui.reusables.RarityColorType
import kiwi.hoonkun.ui.reusables.VariantFilterIcon
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp

@Composable
fun InventoryItems(items: List<Item>, selection: EditorState.SelectionState, noSpaceInInventory: Boolean) {
    val variantFilter = remember { mutableStateOf<String?>(null) }
    val rarityFilter = remember { mutableStateOf<String?>(null) }
    
    val overlays = LocalOverlayState.current

    val datasets by remember(items) {
        derivedStateOf {
            items.filter {
                val variantMatched =
                    (variantFilter.value == null || (if (variantFilter.value == "Enchanted") it.enchanted else it.data.variant == variantFilter.value))
                val rarityMatched = (rarityFilter.value == null || it.rarity == rarityFilter.value)

                variantMatched && rarityMatched
            }
        }
    }

    Row {
        UnequippedItemFilterer(
            variantFilter = variantFilter,
            rarityFilter = rarityFilter,
            onCreateItem = {
                // TODO!
//                if (noSpaceInInventory) Arctic.overlayState.inventoryFull = true
//                else Arctic.overlayState.itemCreation = ItemCreationOverlayState()
            }
        )
        ItemsLazyGrid(items = datasets) { _, item ->
            ItemGridItem(item, selection = selection)
        }
    }
}

@Composable
private fun UnequippedItemFilterer(
    variantFilter: MutableState<String?>,
    rarityFilter: MutableState<String?>,
    onCreateItem: () -> Unit
) {

    var variant by variantFilter
    var rarity by rarityFilter

    val variants = remember { listOf("Melee", "Armor", "Ranged", "Artifact", "Enchanted") }
    val rarities = remember { listOf("Unique", "Rare", "Common") }

    Column(horizontalAlignment = Alignment.End) {
        Spacer(modifier = Modifier.height(12.5.dp))
        for (item in variants) {
            VariantFilterButton(item, item == variant) { variant = if (variant == item) null else item }
        }
        Spacer(modifier = Modifier.height(10.dp))
        for (item in rarities) {
            RarityFilterButton(item, item == rarity) { rarity = if (rarity == item) null else item }
        }
        Spacer(modifier = Modifier.weight(1f))
        AddItemButton(onCreateItem)
        Spacer(modifier = Modifier.height(12.5.dp))
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
            .size(70.dp)
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .padding(vertical = 12.5.dp)
            .padding(end = 12.5.dp, start = 22.5.dp)
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
            .size(70.dp, 40.dp)
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .padding(vertical = 10.dp)
            .drawBehind {
                drawRoundRect(
                    RarityColor(rarity, RarityColorType.Opaque).copy(alpha = alpha),
                    topLeft = Offset(25.dp.toPx(), size.height / 2 - 6.dp.toPx()),
                    size = Size(size.width - 40.dp.toPx(), 12.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
            }
    )
}

@Composable
private fun AddItemButton(onClick: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .size(70.dp)
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .padding(vertical = 12.5.dp)
            .padding(end = 12.5.dp, start = 22.5.dp)
            .drawBehind {
                drawRect(
                    color = Color(if(hovered) 0xffffffff else 0xff79706b),
                    topLeft = Offset(size.width / 2 - 2.dp.toPx(), 9.dp.toPx()),
                    size = Size(4.dp.toPx(), size.height - 18.dp.toPx())
                )
                drawRect(
                    Color(if(hovered) 0xffffffff else 0xff79706b),
                    topLeft = Offset(4.dp.toPx(), size.height / 2 - 2.dp.toPx()),
                    size = Size(size.width - 8.dp.toPx(), 4.dp.toPx())
                )
            }
    )
}
