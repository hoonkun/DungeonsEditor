package arctic.ui.composables.atomic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import arctic.ui.utils.rememberMutableInteractionSource


private const val radius = 8
private const val stroke = 5

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

    val source = rememberMutableInteractionSource()
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
        AutosizeText(text = text, maxFontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
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
                    topLeft = Offset(densityDp(radius), 0f),
                    size = Size(size.width - 2 * densityDp(radius), size.height)
                )
                drawRect(
                    color = color,
                    topLeft = Offset(0f, densityDp(radius)),
                    size = Size(size.width, size.height - 2 * densityDp(radius))
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
                    topLeft = Offset(densityDp(stroke), densityDp(stroke) + densityDp(radius)),
                    size = Size(size.width - 2 * densityDp(stroke), size.height - 2 * (densityDp(stroke) + densityDp(radius)))
                )
                drawRect(
                    color = color,
                    topLeft = Offset(densityDp(stroke) + densityDp(radius), densityDp(stroke)),
                    size = Size(size.width - 2 * (densityDp(stroke) + densityDp(radius)), size.height - 2 * densityDp(stroke))
                )
            }
    )
}
