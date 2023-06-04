package arctic.ui.composables.atomic

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import arctic.ui.unit.dp


@Stable
fun DrawScope.drawInteractionBorder(hovered: Boolean, selected: Boolean) {
    if (!hovered && !selected) return
    drawRoundRect(
        brush = SolidColor(if (selected) Color.White else Color.White.copy(0.35f)),
        cornerRadius = CornerRadius(6.dp.value, 6.dp.value),
        style = Stroke(3.dp.value)
    )
}

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
        topLeft = Offset(0f, size.height / 2 - 3.dp.value),
        size = Size(size.width, 6.dp.value)
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
    drawRect(
        Color.White,
        alpha = alpha,
        style = Stroke(width = 6.dp.value)
    )
}
