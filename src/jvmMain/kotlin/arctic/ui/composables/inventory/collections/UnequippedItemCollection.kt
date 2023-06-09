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
import arctic.states.*
import arctic.ui.composables.atomic.RarityColor
import arctic.ui.composables.atomic.RarityColorType
import arctic.ui.composables.atomic.VariantFilterIcon
import arctic.ui.composables.atomic.densityDp
import arctic.ui.unit.dp
import arctic.ui.utils.rememberMutableInteractionSource
import dungeons.states.Item

@Composable
fun UnequippedItemCollection(items: List<Item>, selection: EditorSelectionState, noSpaceInInventory: Boolean) {
    val variantFilter = remember { mutableStateOf<String?>(null) }
    val rarityFilter = remember { mutableStateOf<String?>(null) }

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
                if (noSpaceInInventory) Arctic.overlayState.inventoryFull = true
                else Arctic.overlayState.itemCreation = ItemCreationOverlayState()
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

    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(top = 15.dp)
    ) {
        for (item in variants) {
            VariantFilterButton(item, item == variant) { variant = if (variant == item) null else item }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        for (item in rarities) {
            RarityFilterButton(item, item == rarity) { rarity = if (rarity == item) null else item }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        AddItemButton(onCreateItem)
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
            .size(60.dp, 40.dp)
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    RarityColor(rarity, RarityColorType.Opaque).copy(alpha = alpha),
                    topLeft = Offset(densityDp(10), size.height / 2 - densityDp(6)),
                    size = Size(size.width - densityDp(20), densityDp(12)),
                    cornerRadius = CornerRadius(densityDp(3))
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
            .size(60.dp)
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .padding(10.dp)
            .drawBehind {
                drawRect(
                    color = Color(if(hovered) 0xffffffff else 0xff79706b),
                    topLeft = Offset(size.width / 2 - densityDp(2), densityDp(8)),
                    size = Size(densityDp(4), size.height - densityDp(16))
                )
                drawRect(
                    Color(if(hovered) 0xffffffff else 0xff79706b),
                    topLeft = Offset(densityDp(8), size.height / 2 - densityDp(2)),
                    size = Size(size.width - densityDp(16), densityDp(4))
                )
            }
    )
}
