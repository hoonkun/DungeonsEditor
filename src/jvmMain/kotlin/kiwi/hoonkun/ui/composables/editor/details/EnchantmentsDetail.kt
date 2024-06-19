package kiwi.hoonkun.ui.composables.editor.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import kiwi.hoonkun.ui.composables.base.EnchantmentIconImage
import kiwi.hoonkun.ui.composables.base.EnchantmentLevelImage
import kiwi.hoonkun.ui.composables.overlays.EnchantmentOverlay
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.states.Enchantment
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures


@Composable
fun ItemEnchantments(enchantments: List<Enchantment>) {
    val slots = enchantments.chunked(3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(3f / 1f)
    ) {
        Image(
            bitmap = DungeonsTextures["/Game/UI/Inventory/Runes.png"],
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .scale(2.25f)
                .alpha(0.5f)
                .offset(y = (-20).dp)
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
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f / 1f)
                .scale(1.15f)
        ) {
            EnchantmentIconImage(
                enchantment = activatedEnchantment
            )
            EnchantmentLevelImage(
                level = activatedEnchantment.level,
                scale = 1.2f,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    } else {
        OpenedSlot(
            first = e0, second = e1, third = e2,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f / 1f)
                .scale(1.07f)
        )
    }
}

@Composable
private fun EnchantmentIconImage(enchantment: Enchantment, modifier: Modifier = Modifier) {
    val overlays = LocalOverlayState.current

    EnchantmentIconImage(
        data = enchantment.data,
        modifier = modifier,
        onClick = {
            overlays.make(
                enter = defaultFadeIn(),
                exit = defaultFadeOut(),
                content = { EnchantmentOverlay(enchantment) }
            )
        }
    )
}

@Composable
private fun SlotTopIcon(modifier: Modifier = Modifier) =
    Image(
        bitmap = DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment2/enchant_icon.png"],
        contentDescription = null,
        modifier = modifier.scale(0.375f)
    )

@Composable
private fun OpenedSlot(
    first: Enchantment,
    second: Enchantment,
    third: Enchantment,
    modifier: Modifier = Modifier
) =
    Box(modifier = modifier) {
        val sizeModifier = Modifier.fillMaxSize(0.5f)

        SlotTopIcon(modifier = sizeModifier.offsetRelative(0.5f, 0f))
        EnchantmentIconImage(first, modifier = sizeModifier.offsetRelative(0f, 0.5f))
        EnchantmentIconImage(second, modifier = sizeModifier.offsetRelative(0.5f, 1f))
        EnchantmentIconImage(third, modifier = sizeModifier.offsetRelative(1f, 0.5f))
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
