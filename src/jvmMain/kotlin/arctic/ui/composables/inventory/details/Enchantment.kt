package arctic.ui.composables.inventory.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import arctic.states.arctic
import arctic.ui.composables.atomic.EnchantmentIconImage
import arctic.ui.composables.atomic.EnchantmentLevelImage
import dungeons.IngameImages
import dungeons.states.Enchantment
import dungeons.states.extensions.data


@Composable
fun ItemEnchantments(enchantments: List<Enchantment>) {
    val slots = remember(enchantments) { enchantments.chunked(3) }

    Row(modifier = Modifier.fillMaxSize()) {
        for (slot in slots) {
            ItemEnchantmentSlot(slot)
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
            EnchantmentLevelImage(activatedEnchantment.level)
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
        selected = arctic.enchantments.detailTarget == enchantment,
        modifier = Modifier.fillMaxSize(),
        onClick = { arctic.enchantments.viewDetail(enchantment) }
    )
}

@Composable
fun SlotTopIcon() =
    Image(
        bitmap = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment2/enchant_icon.png" },
        contentDescription = null,
        modifier = Modifier.fillMaxSize().scale(0.5f)
    )

@Composable
private fun RowScope.ActivatedSlot(content: @Composable BoxScope.() -> Unit) =
    Box(modifier = Modifier.weight(1f).aspectRatio(1f / 1f), content = content)

@Composable
private fun RowScope.OpenedSlot(
    top: @Composable BoxScope.() -> Unit,
    right: @Composable BoxScope.() -> Unit,
    bottom: @Composable BoxScope.() -> Unit,
    left: @Composable BoxScope.() -> Unit,
) =
    Box(modifier = Modifier.weight(1f).aspectRatio(1f / 1f)) {
        Box(
            modifier = Modifier.fillMaxSize(0.5f).offsetRelative(0.5f, 0.5f, 0f, 0f),
            content = top
        )
        Box(
            modifier = Modifier.fillMaxSize(0.5f).offsetRelative(0f, 0f, 0.5f, 0.5f),
            content = left
        )
        Box(
            modifier = Modifier.fillMaxSize(0.5f).offsetRelative(0.5f, 0.5f, 1f, 1f),
            content = bottom
        )
        Box(
            modifier = Modifier.fillMaxSize(0.5f).offsetRelative(1f, 1f, 0.5f, 0.5f),
            content = right
        )
    }

private fun Modifier.offsetRelative(
    x: Float = 0f,
    xAnchor: Float = 0.5f,
    y: Float = 0f,
    yAnchor: Float = 0.5f
): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(
                x = (constraints.maxWidth * x - placeable.width * xAnchor).toInt(),
                y = (constraints.maxHeight * y - placeable.height * yAnchor).toInt()
            )
        }
    }
