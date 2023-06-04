package arctic.ui.composables.atomic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import arctic.ui.unit.dp
import arctic.ui.unit.sp


private val radius = 8.dp.value
private val stroke = 5.dp.value

@Composable
fun RetroButton(
    text: String,
    color: Color,
    hoverInteraction: String,
    disabledColor: Color = Color(0xff666666),
    enabled: Boolean = true,
    buttonSize: Pair<Dp, Dp> = 225.dp to 70.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val pressed by source.collectIsPressedAsState()

    val solidColor = if (enabled) color else disabledColor

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(buttonSize.first, buttonSize.second)
            .hoverable(source, enabled)
            .clickable(source, null, enabled, onClick = onClick).then(modifier)
    ) {
        if (hoverInteraction == "outline") {
            if (hovered) Outline(Color.White)
            Solid(solidColor)
            if (pressed) Outline(Color.Black, alpha = 0.25f)
        } else if (hoverInteraction == "overlay") {
            if (hovered) Solid(solidColor, alpha = 0.2f)
            if (pressed) Solid(Color.Black, alpha = 0.25f)
        }
        Text(text = text, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Outline(color: Color, alpha: Float = 1f) {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .drawBehind {
                drawRect(
                    color = color,
                    topLeft = Offset(radius, 0f),
                    size = Size(size.width - 2 * radius, size.height)
                )
                drawRect(
                    color = color,
                    topLeft = Offset(0f, radius),
                    size = Size(size.width, size.height - 2 * radius)
                )
            }
    )
}

@Composable
private fun Solid(color: Color, alpha: Float = 1f) {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .drawBehind {
                drawRect(
                    color = color,
                    topLeft = Offset(stroke, stroke + radius),
                    size = Size(size.width - 2 * stroke, size.height - 2 * (stroke + radius))
                )
                drawRect(
                    color = color,
                    topLeft = Offset(stroke + radius, stroke),
                    size = Size(size.width - 2 * (stroke + radius), size.height - 2 * stroke)
                )
            }
    )
}
