package composable.inventory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import extensions.GameResources
import states.*

@Composable
fun ItemEnchantmentsView(item: Item, slots: List<EnchantmentSlot>) {
    Spacer(modifier = Modifier.height(40.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        for (slot in slots) {
            ItemEnchantmentSlotView(slot)
        }
    }
}

@Composable
private fun RowScope.ItemEnchantmentSlotView(slot: EnchantmentSlot) {
    val activatedEnchantment = slot.activatedEnchantment
    val (e0, e1, e2) = slot.enchantments

    if (activatedEnchantment != null) {
        ActivatedSlot {
            EnchantmentIcon(activatedEnchantment)
            LevelImagePositioner { LevelImage(activatedEnchantment) }
        }
    } else if (e0.id == "Unset" && e1.id == "Unset" && e2.id == "Unset") {
        LockedSlot()
    } else {
        OpenedSlot(
            top = { SlotTopIcon() },
            right = { EnchantmentIcon(e1) },
            left = { EnchantmentIcon(e0) },
            bottom = { EnchantmentIcon(e2) }
        )
    }
}

@Composable
fun RowScope.OpenedSlot(
    top: @Composable RowScope.() -> Unit,
    right: @Composable RowScope.() -> Unit,
    bottom: @Composable RowScope.() -> Unit,
    left: @Composable RowScope.() -> Unit,
) =
    Column(modifier = Modifier.weight(1f).aspectRatio(1f / 1f).rotate(45f).scale(0.75f)) {
        Row {
            top()
            right()
        }
        Row {
            left()
            bottom()
        }
    }

@Composable
fun RowScope.ActivatedSlot(content: @Composable BoxScope.() -> Unit) =
    Box(Modifier.weight(1f).scale(1.1f).aspectRatio(1f / 1f), content = content)

@Composable
fun BoxScope.LevelImagePositioner(content: @Composable BoxScope.() -> Unit) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(0.45f).align(Alignment.TopEnd),
        content = content
    )

@Composable
fun LevelImage(enchantment: Enchantment) =
    Image(
        GameResources.image { "/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${enchantment.level}_normal_text.png" },
        null,
        modifier = Modifier.size(80.dp)
    )

@Composable
fun RowScope.LockedSlot() =
    Image(
        bitmap = GameResources.image { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_row.png" },
        contentDescription = null,
        modifier = Modifier.weight(1f).scale(1.1f)
    )

@Composable
fun RowScope.SlotTopIcon() =
    Image(
        bitmap = GameResources.image { "/Game/UI/Materials/Inventory2/Enchantment2/enchant_icon.png" },
        contentDescription = null,
        modifier = Modifier.weight(1f).aspectRatio(1f).scale(0.5f).rotate(-45f)
    )

@Composable
fun RowScope.EnchantmentIcon(enchantment: Enchantment) =
    BlurBehindImage(
        bitmap = enchantment.Image(),
        enabled = enchantment.id != "Unset",
        modifier = Modifier.weight(1f).aspectRatio(1f).rotate(-45f).scale(enchantment.ImageScale())
    )

@Composable
fun BoxScope.EnchantmentIcon(enchantment: Enchantment) =
    BlurBehindImage(
        bitmap = enchantment.Image(),
        modifier = Modifier.fillMaxSize().align(Alignment.Center)
    )

@Composable
private fun BlurBehindImage(bitmap: ImageBitmap, enabled: Boolean = true, modifier: Modifier = Modifier) =
    Box(modifier = modifier) {
        if (enabled) Image(bitmap, null, modifier = Modifier.fillMaxSize().scale(1.05f).blur(10.dp), alpha = 0.85f)
        Image(bitmap, null, modifier = Modifier.fillMaxSize())
    }
