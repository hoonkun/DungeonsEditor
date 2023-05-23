package composable.inventory

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import editorState
import extensions.GameResources
import states.*

@Composable
fun ItemEnchantmentsView(slots: List<EnchantmentSlot>) {
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
        ActivatedSlot(activatedEnchantment) {
            EnchantmentIcon(activatedEnchantment, true)
            LevelImagePositioner { LevelImage(activatedEnchantment.level, 1.75f) }
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
fun RowScope.ActivatedSlot(enchantment: Enchantment, content: @Composable BoxScope.() -> Unit) {
    val source = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f / 1f)
            .clickable(source, null) { editorState.detailState.selectEnchantment(enchantment) },
        content = content
    )
}

@Composable
fun BoxScope.LevelImagePositioner(size: Float = 0.3f, content: @Composable BoxScope.() -> Unit) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(size).align(Alignment.TopEnd),
        content = content
    )

@Composable
fun LevelImage(level: Int, scale: Float = 1.0f) {
    if (level == 0) return

    Image(
        GameResources.image { "/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${level}_normal_text.png" },
        null,
        modifier = Modifier.fillMaxSize(0.7f).scale(scale)
    )
}

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
fun RowScope.EnchantmentIcon(enchantment: Enchantment) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val selected = editorState.detailState.selectedEnchantment == enchantment

    BlurBehindImage(
        bitmap = enchantment.Image(),
        enabled = enchantment.id != "Unset",
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clickable(source, null) { editorState.detailState.selectEnchantment(enchantment) }
            .hoverable(source)
            .rotate(-45f)
            .scale(enchantment.ImageScale())
            .rotate(45f)
            .drawBehind {
                drawRect(
                    if (selected) Color.White else if (hovered) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                    style = Stroke(width = 4.dp.value),
                    size = Size(size.width * 0.6f, size.height * 0.6f),
                    topLeft = Offset(size.width * 0.2f, size.height * 0.2f)
                )
            }
            .rotate(-45f)
    )
}

@Composable
fun BoxScope.EnchantmentIcon(enchantment: Enchantment, indicatorEnabled: Boolean = false, scale: Float = 1.2f) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val selected = editorState.detailState.selectedEnchantment == enchantment
    BlurBehindImage(
        bitmap = enchantment.Image(),
        scale = scale,
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center)
            .hoverable(source)
            .rotate(45f)
            .drawBehind {
                if (!indicatorEnabled) return@drawBehind

                drawRect(
                    if (selected) Color.White else if (hovered) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                    style = Stroke(width = 6.dp.value),
                    size = Size(size.width * 0.69f, size.height * 0.69f),
                    topLeft = Offset(size.width * 0.155f, size.height * 0.155f)
                )
            }
            .rotate(-45f)
    )
}

@Composable
fun BlurBehindImage(bitmap: ImageBitmap, alpha: Float = 1.0f, scale: Float = 1.0f, enabled: Boolean = true, modifier: Modifier = Modifier) =
    Box(modifier = modifier) {
        if (enabled) Image(bitmap, null, modifier = Modifier.fillMaxSize().scale(scale + 0.05f).blur(10.dp), alpha = 0.85f * alpha)
        Image(bitmap, null, alpha = alpha, modifier = Modifier.fillMaxSize().scale(scale))
    }
