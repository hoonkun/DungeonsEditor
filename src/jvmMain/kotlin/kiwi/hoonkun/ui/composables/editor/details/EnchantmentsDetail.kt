package kiwi.hoonkun.ui.composables.editor.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import kiwi.hoonkun.ui.composables.base.EnchantmentImage
import kiwi.hoonkun.ui.composables.base.EnchantmentLevel
import kiwi.hoonkun.ui.composables.base.EnchantmentSlot
import kiwi.hoonkun.ui.composables.base.MutableEnchantments
import kiwi.hoonkun.ui.composables.overlays.EnchantmentOverlay
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.reusables.drawEnchantmentRune
import kiwi.hoonkun.ui.states.LocalOverlayState
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.data


@Composable
fun ItemEnchantments(
    holder: MutableDungeons.Item,
    enchantments: MutableEnchantments,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .aspectRatio(3f / 1f)
            .drawBehind { drawEnchantmentRune() }
    ) {
        val slots = remember(enchantments) { enchantments.all.chunked(3) }
        for (slot in slots) {
            ItemEnchantmentEach(
                holder = holder,
                slot = MutableEnchantments(slot),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f / 1f)
            )
        }
    }
}

@Composable
private fun ItemEnchantmentEach(
    holder: MutableDungeons.Item,
    slot: MutableEnchantments,
    modifier: Modifier = Modifier
) {
    val activatedEnchantment = slot.all.find { it.level > 0 }

    val overlays = LocalOverlayState.current
    val makeEnchantmentOverlay: (MutableDungeons.Enchantment) -> Unit = { enchantment ->
        overlays.make(
            enter = defaultFadeIn(),
            exit = defaultFadeOut(),
            content = {
                EnchantmentOverlay(
                    holder = holder,
                    initialSelected = enchantment,
                    requestClose = it
                )
            }
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
            enchantments = slot,
            modifier = Modifier.scale(1.07f).then(modifier)
        ) { enchantment ->
            EnchantmentImage(data = enchantment.data) { makeEnchantmentOverlay(enchantment) }
        }
    }
}
