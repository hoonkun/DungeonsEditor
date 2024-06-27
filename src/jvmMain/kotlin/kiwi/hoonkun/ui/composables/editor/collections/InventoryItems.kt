package kiwi.hoonkun.ui.composables.editor.collections

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import kiwi.hoonkun.ui.composables.overlays.InventoryFullOverlay
import kiwi.hoonkun.ui.composables.overlays.ItemOverlay
import kiwi.hoonkun.ui.composables.overlays.ItemOverlayCreateState
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp

@Composable
fun InventoryItems(
    items: List<Item>,
    editorState: EditorState
) {
    var filters by remember { mutableStateOf(InventoryItemFilter()) }

    val selection = editorState.selection
    val overlays = LocalOverlayState.current

    val datasets by remember(items) {
        derivedStateOf {
            val (variant, rarity) = filters
            items.filter filter@ {
                if (rarity == null && variant == null) return@filter true

                val variantMatched = variant == null || if (variant == "Enchanted") it.enchanted else it.data.variant == variant
                val rarityMatched = rarity == null || it.rarity == rarity

                variantMatched && rarityMatched
            }
        }
    }

    Row {
        UnequippedItemFilterer(
            filters = filters,
            onFilterChange = { filters = it },
            onCreateItem = {
                if (editorState.noSpaceInInventory)
                    overlays.make { InventoryFullOverlay() }
                else {
                    overlays.make(
                        enter = defaultFadeIn(),
                        exit = defaultFadeOut()
                    ) {
                        ItemOverlay(
                            state = remember { ItemOverlayCreateState(editorState) },
                            requestClose = it
                        )
                    }
                }
            }
        )
        ItemsLazyGrid(
            items = datasets,
            itemContent = { item -> ItemGridItem(item, selection = selection) }
        )
    }
}

@Composable
private fun UnequippedItemFilterer(
    filters: InventoryItemFilter,
    onFilterChange: (InventoryItemFilter) -> Unit,
    onCreateItem: () -> Unit
) {
    val (selectedVariant, selectedRarity) = filters

    val variants = remember { listOf("Melee", "Armor", "Ranged", "Artifact", "Enchanted") }
    val rarities = remember { listOf("Unique", "Rare", "Common") }

    Column(horizontalAlignment = Alignment.End) {
        Spacer(modifier = Modifier.height(12.5.dp))
        for (variant in variants) {
            VariantFilterButton(
                variant = variant,
                selected = variant == selectedVariant,
                onClick = { onFilterChange(filters.copy(variant = if (selectedVariant == variant) null else variant)) }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        for (rarity in rarities) {
            RarityFilterButton(
                rarity = rarity,
                selected = rarity == selectedRarity,
                onClick = { onFilterChange(filters.copy(rarity = if (selectedRarity == rarity) null else rarity)) }
            )
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

    Image(
        bitmap = VariantFilterIcon(variant, selected || hovered),
        contentDescription = null,
        modifier = Modifier
            .size(70.dp)
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .padding(vertical = 12.5.dp)
            .padding(end = 12.5.dp, start = 22.5.dp)
            .graphicsLayer { alpha = if (selected) 1f else if (hovered) 0.55f else 0.35f }
    )
}

@Composable
private fun RarityFilterButton(rarity: String, selected: Boolean, onClick: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Canvas(
        modifier = Modifier
            .size(70.dp, 40.dp)
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .padding(vertical = 10.dp)
            .graphicsLayer { alpha = if (selected) 1f else if (hovered) 0.55f else 0.35f }
    ) {
        drawRoundRect(
            RarityColor(rarity, RarityColorType.Opaque),
            topLeft = Offset(25.dp.toPx(), size.height / 2 - 6.dp.toPx()),
            size = Size(size.width - 40.dp.toPx(), 12.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
    }
}

@Composable
private fun AddItemButton(onClick: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Canvas(
        modifier = Modifier
            .size(70.dp)
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .padding(vertical = 12.5.dp)
            .padding(end = 12.5.dp, start = 22.5.dp)
    ) {
        val color = Color(if(hovered) 0xffffffff else 0xff79706b)
        val draw: DrawScope.() -> Unit = {
            drawRect(
                color = color,
                topLeft = Offset(size.width / 2 - 2.dp.toPx(), 9.dp.toPx()),
                size = Size(4.dp.toPx(), size.height - 18.dp.toPx())
            )
        }
        draw()
        rotate(degrees = 90f, block = draw)
    }
}

private data class InventoryItemFilter(
    val variant: String? = null,
    val rarity: String? = null,
)
