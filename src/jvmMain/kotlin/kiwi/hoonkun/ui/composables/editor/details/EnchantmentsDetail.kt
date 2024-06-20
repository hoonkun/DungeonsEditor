package kiwi.hoonkun.ui.composables.editor.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import kiwi.hoonkun.ui.composables.base.EnchantmentImage
import kiwi.hoonkun.ui.composables.base.EnchantmentLevel
import kiwi.hoonkun.ui.composables.base.EnchantmentSlot
import kiwi.hoonkun.ui.composables.overlays.EnchantmentOverlay
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.reusables.drawEnchantmentRune
import kiwi.hoonkun.ui.states.Enchantment
import kiwi.hoonkun.ui.states.LocalOverlayState


@Immutable
data class EnchantmentsHolder(
    val all: List<Enchantment>
)

@Composable
fun ItemEnchantments(
    enchantments: EnchantmentsHolder,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .aspectRatio(3f / 1f)
            .drawBehind { drawEnchantmentRune() }
    ) {
        val slots = remember { enchantments.all.chunked(3) }
        for (slot in slots) {
            ItemEnchantmentEach(
                slot = EnchantmentsHolder(slot),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f / 1f)
            )
        }
    }
}

@Composable
private fun ItemEnchantmentEach(
    slot: EnchantmentsHolder,
    modifier: Modifier = Modifier
) {
    val activatedEnchantment = slot.all.find { it.level > 0 }
    val (e0, e1, e2) = slot.all

    val overlays = LocalOverlayState.current
    val makeEnchantmentOverlay: (Enchantment) -> Unit = { enchantment ->
        overlays.make(
            enter = defaultFadeIn(),
            exit = defaultFadeOut(),
            content = { EnchantmentOverlay(original = enchantment, requestClose = { overlays.destroy(it) }) }
        )
    }

    if (activatedEnchantment != null) {
        Box(modifier = Modifier.scale(1.15f).then(modifier)) {
            EnchantmentImage(
                data = activatedEnchantment.data,
                modifier = Modifier.fillMaxSize(),
                onClick = { makeEnchantmentOverlay(activatedEnchantment) }
            )
            EnchantmentLevel(
                level = activatedEnchantment.level,
                scale = 1.2f,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    } else {
        EnchantmentSlot(
            first = { EnchantmentImage(e0.data) { makeEnchantmentOverlay(e0) } },
            second = { EnchantmentImage(e1.data) { makeEnchantmentOverlay(e1) } },
            third = { EnchantmentImage(e2.data) { makeEnchantmentOverlay(e2) } },
            modifier = Modifier.scale(1.07f).then(modifier)
        )
    }
}
