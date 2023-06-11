package arctic.ui.composables.inventory.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import arctic.states.Arctic
import arctic.states.ItemEnchantmentOverlayState
import arctic.ui.composables.atomic.EnchantmentIconImage
import arctic.ui.composables.atomic.EnchantmentLevelImage
import arctic.ui.unit.dp
import dungeons.IngameImages
import dungeons.states.Enchantment


@Composable
fun ItemEnchantments(enchantments: List<Enchantment>) {
    val slots = enchantments.chunked(3)

    Box(modifier = Modifier.fillMaxSize().aspectRatio(3f / 1f)) {
        Image(
            bitmap = IngameImages.get { "/Game/UI/Inventory/Runes.png" },
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().scale(2.25f).alpha(0.5f).offset(y = (-20).dp)
        )
        Row(modifier = Modifier.fillMaxSize()) {
            for (slot in slots) {
                ItemEnchantmentSlot(slot)
            }
        }
    }
}

@Composable
private fun RowScope.ItemEnchantmentSlot(slot: List<Enchantment>) {
    val activatedEnchantment = slot.find { it.level > 0 }
    val (e0, e1, e2) = slot

    if (activatedEnchantment != null) {
        ActivatedSlot {
            EnchantmentIconImage(activatedEnchantment)
            EnchantmentLevelImage(activatedEnchantment.level, scale = 1.2f)
        }
    } else {
        OpenedSlot(
            top = { SlotTopIcon() },
            left = { EnchantmentIconImage(e0) },
            bottom = { EnchantmentIconImage(e1) },
            right = { EnchantmentIconImage(e2) }
        )
    }
}

@Composable
fun EnchantmentIconImage(enchantment: Enchantment) {
    EnchantmentIconImage(
        data = enchantment.data,
        modifier = Modifier.fillMaxSize(),
        onClick = { Arctic.overlayState.enchantment = ItemEnchantmentOverlayState(enchantment.holder, enchantment) }
    )
}

@Composable
fun SlotTopIcon() =
    Image(
        bitmap = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment2/enchant_icon.png" },
        contentDescription = null,
        modifier = Modifier.fillMaxSize().scale(0.375f)
    )

@Composable
private fun RowScope.ActivatedSlot(content: @Composable BoxScope.() -> Unit) =
    Box(modifier = Modifier.weight(1f).aspectRatio(1f / 1f).scale(1.15f), content = content)

@Composable
private fun RowScope.OpenedSlot(
    top: @Composable BoxScope.() -> Unit,
    right: @Composable BoxScope.() -> Unit,
    bottom: @Composable BoxScope.() -> Unit,
    left: @Composable BoxScope.() -> Unit,
) =
    Box(modifier = Modifier.weight(1f).aspectRatio(1f / 1f).scale(1.07f)) {
        Box(
            modifier = Modifier.fillMaxSize(0.5f).offsetRelative(0.5f, 0f),
            content = top
        )
        Box(
            modifier = Modifier.fillMaxSize(0.5f).offsetRelative(0f, 0.5f),
            content = left
        )
        Box(
            modifier = Modifier.fillMaxSize(0.5f).offsetRelative(0.5f, 1f),
            content = bottom
        )
        Box(
            modifier = Modifier.fillMaxSize(0.5f).offsetRelative(1f, 0.5f),
            content = right
        )
    }

private fun Modifier.offsetRelative(
    x: Float = 0f,
    y: Float = 0f,
): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(
                x = (constraints.maxWidth * x).toInt(),
                y = (constraints.maxHeight * y).toInt()
            )
        }
    }
