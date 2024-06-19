package kiwi.hoonkun.ui.composables.editor.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import kiwi.hoonkun.ui.composables.base.EnchantmentIconImage
import kiwi.hoonkun.ui.composables.base.EnchantmentLevelImage
import kiwi.hoonkun.ui.composables.overlays.EnchantmentOverlay
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.reusables.offsetRelative
import kiwi.hoonkun.ui.states.Enchantment
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures


@Immutable
data class EnchantmentSlots(
    val all: List<Enchantment>
) {
    val slots = all.chunked(3)
}

@Immutable
data class EnchantmentSlot(val all: List<Enchantment>)

@Composable
fun ItemEnchantments(
    enchantments: EnchantmentSlots,
    modifier: Modifier = Modifier,
    breakdown: Boolean = false,
    highlight: Enchantment? = null,
    readonly: Boolean = false,
) {
    Box(
        modifier = modifier
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
            for (slot in enchantments.slots) {
                ItemEnchantmentSlot(
                    slot = EnchantmentSlot(slot),
                    breakdown = breakdown,
                    highlight = highlight,
                    readonly = readonly
                )
            }
        }
    }
}

@Composable
private fun RowScope.ItemEnchantmentSlot(
    slot: EnchantmentSlot,
    breakdown: Boolean = false,
    highlight: Enchantment? = null,
    readonly: Boolean = false,
) {
    val activatedEnchantment = slot.all.find { it.level > 0 }
    val (e0, e1, e2) = slot.all

    if (!breakdown && activatedEnchantment != null) {
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f / 1f)
                .scale(1.15f)
        ) {
            EnchantmentIconImage(
                enchantment = activatedEnchantment,
                disabled = readonly
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
            disabled = readonly,
            highlight = highlight,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f / 1f)
                .scale(1.07f)
        )
    }
}

@Composable
private fun EnchantmentIconImage(
    enchantment: Enchantment,
    disabled: Boolean = false,
    forceOpaque: Boolean = false,
    outline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val overlays = LocalOverlayState.current

    EnchantmentIconImage(
        data = enchantment.data,
        modifier = modifier,
        forceOpaque = forceOpaque,
        outline = outline,
        disabled = disabled,
        onClick = {
            overlays.make(
                enter = defaultFadeIn(),
                exit = defaultFadeOut(),
                content = { EnchantmentOverlay(original = enchantment, requestClose = { overlays.destroy(it) }) }
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
    modifier: Modifier = Modifier,
    highlight: Enchantment? = null,
    disabled: Boolean = false,
) =
    Box(modifier = modifier) {
        val sizeModifier = Modifier.fillMaxSize(0.5f)

        SlotTopIcon(modifier = sizeModifier.offsetRelative(0.5f, 0f))
        listOf(first to Offset(0f, 0.5f), second to Offset(0.5f, 1f), third to Offset(1f, 0.5f))
            .forEach { (enchantment, offset) ->
                val haveToHighlight = highlight === enchantment
                EnchantmentIconImage(
                    enchantment = enchantment,
                    disabled = disabled,
                    forceOpaque = true,
                    outline = haveToHighlight,
                    modifier = sizeModifier
                        .offsetRelative(offset.x, offset.y)
                        .scale(if (haveToHighlight) 1.25f else 1f)
                        .graphicsLayer { alpha = if (highlight == null || haveToHighlight) 1f else 0.5f }
                )
            }
    }
