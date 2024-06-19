package kiwi.hoonkun.ui.reusables

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures


@Stable
fun DrawScope.drawInteractionBorder(hovered: Boolean, selected: Boolean = false) {
    if (!hovered && !selected) return

    drawRoundRect(
        brush = SolidColor(Color.White.copy(alpha = if (selected) 1f else 0.35f)),
        cornerRadius = CornerRadius(6.dp.toPx()),
        style = Stroke(3.dp.toPx())
    )
}

@Stable
fun DrawScope.drawItemFrame(
    rarity: String,
    glided: Boolean = false,
    enchanted: Boolean = false,
    isArtifact: Boolean = false
) {
    val backName =
        when(rarity) {
            "Common" -> "main"
            "Rare" -> "rare"
            "Unique" -> "unique"
            else -> "main"
        }

    if (glided) drawRect(GlidedItemBackgroundGradient())
    if (enchanted) drawRect(EnchantmentPointsBackgroundGradient())

    drawImage(
        image = DungeonsTextures["/Game/UI/Materials/Inventory2/Slot/v2_${if (isArtifact) "item" else "gear"}_${backName}_slot.png"],
        srcSize = IntSize(322, 322),
        dstSize = IntSize(size.width.toInt(), size.height.toInt()),
        alpha = 0.75f
    )
    if (rarity == "Rare" || rarity == "Unique") {
        drawImage(
            image = DungeonsTextures["/Game/UI/Materials/Inventory2/Slot/v2_${backName}_overlay.png"],
            srcSize = IntSize(322, 322),
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            alpha = 0.75f
        )
    }
}

@Stable
private fun PowerBackgroundGradient() =
    Brush.linearGradient(0f to Color.Transparent, 0.5f to Color.Transparent, 1f to Color(0x70000000))

@Stable
private fun EnchantmentPointsBackgroundGradient() =
    Brush.linearGradient(
        0f to Color.Transparent, 0.6f to Color.Transparent, 1f to Color(0x60b442f6),
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    )

@Stable
private fun GlidedItemBackgroundGradient() =
    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent, Color(0xaaffc847)))

@Stable
private fun RarityBackgroundGradient(rarity: String) =
    Brush.linearGradient(listOf(RarityColor(rarity, RarityColorType.Translucent), Color.Transparent))

@Stable
private fun RarityBorderGradient1(rarity: String) =
    Brush.linearGradient(listOf(RarityColor(rarity, RarityColorType.Opaque), Color.Transparent, RarityColor(rarity, RarityColorType.Opaque).copy(alpha = 0.75f)))

@Stable
private fun RarityBorderGradient2(rarity: String, width: Float, height: Float) =
    Brush.linearGradient(listOf(RarityColor(rarity, RarityColorType.Opaque).copy(alpha = 0.5f), Color.Transparent), start = Offset(width, 0f), end = Offset(0f, height))


@Stable
fun DrawScope.drawUniqueIndicator() {
    val color = RarityColor("Unique", RarityColorType.Opaque)
    drawRect(
        Brush.linearGradient(
            0f to color.copy(alpha = 0f),
            0.5f to color.copy(alpha = 0.75f),
            1f to color.copy(alpha = 0f),
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f)
        ),
        topLeft = Offset(0f, size.height / 2 - 3.dp.toPx()),
        size = Size(size.width, 6.dp.toPx())
    )
    drawCircle(
        Brush.radialGradient(
            0f to color.copy(alpha = 0.45f),
            1f to color.copy(alpha = 0f),
            center = Offset(size.width / 2f, size.height / 2f),
        )
    )
}

@Stable
fun DrawScope.drawEnchantmentIconBorder(alpha: Float) {
    val scale = 0.825f

    drawRect(
        color = Color.White,
        alpha = alpha,
        topLeft = Offset(size.width * (1f - scale) * 0.5f, size.height * (1f - scale) * 0.5f),
        size = Size(size.width * scale, size.height * scale),
        style = Stroke(width = 6.dp.toPx())
    )
}
