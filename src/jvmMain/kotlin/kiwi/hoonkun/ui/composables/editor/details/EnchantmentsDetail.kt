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
import kiwi.hoonkun.ui.composables.overlays.EnchantmentOverlay
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.reusables.drawEnchantmentRune
import kiwi.hoonkun.ui.states.LocalOverlayState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.skeleton


@Composable
fun ItemEnchantments(
    holder: MutableDungeons.Item,
    enchantments: ImmutableList<MutableDungeons.Enchantment>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .aspectRatio(3f / 1f)
            .drawBehind { drawEnchantmentRune() }
    ) {
        val slots = remember(enchantments) { enchantments.chunked(3).map { it.toImmutableList() } }
        for (slot in slots) {
            ItemEnchantmentEach(
                holder = holder,
                slot = slot,
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
    slot: ImmutableList<MutableDungeons.Enchantment>,
    modifier: Modifier = Modifier
) {
    val activatedEnchantment = slot.find { it.level > 0 }

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
                data = activatedEnchantment.skeleton,
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
            EnchantmentImage(data = enchantment.skeleton) { makeEnchantmentOverlay(enchantment) }
        }
    }
}
