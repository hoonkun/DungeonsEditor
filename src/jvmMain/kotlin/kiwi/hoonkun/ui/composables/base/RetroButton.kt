package kiwi.hoonkun.ui.composables.base

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp

@Composable
fun RetroButton(
    text: String,
    color: Color,
    hoverInteraction: String,
    disabledColor: Color = Color(0xff666666),
    enabled: Boolean = true,
    buttonSize: Pair<Dp, Dp> = 225.dp to 70.dp,
    fontFamily: FontFamily = FontFamily.Default,
    radius: Dp = 8.dp,
    stroke: Dp = 5.dp,
    bold: Boolean = true,
    maxFontSize: TextUnit = 24.sp,
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
            .then(modifier)
            .size(buttonSize.first, buttonSize.second)
            .hoverable(source, enabled)
            .clickable(source, null, enabled, onClick = onClick)
    ) {
        if (hoverInteraction == "outline") {
            if (hovered) Outline(Color.White, radius = radius)
            Solid(solidColor, radius = radius, stroke = stroke)
            if (pressed) Outline(Color.Black, alpha = 0.25f, radius = radius)
        } else if (hoverInteraction == "overlay") {
            if (hovered) Solid(solidColor, alpha = 0.2f, radius = radius, stroke = stroke)
            if (pressed) Solid(Color.Black, alpha = 0.25f, radius = radius, stroke = stroke)
        }
        Text(
            text = text,
            fontSize = maxFontSize,
            fontFamily = fontFamily,
            color = Color.White,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun Outline(color: Color, alpha: Float = 1f, radius: Dp) {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .drawBehind {
                drawRect(
                    color = color,
                    topLeft = Offset(radius.toPx(), 0f),
                    size = Size(size.width - 2 * radius.toPx(), size.height)
                )
                drawRect(
                    color = color,
                    topLeft = Offset(0f, radius.toPx()),
                    size = Size(size.width, size.height - 2 * radius.toPx())
                )
            }
    )
}

@Composable
private fun Solid(color: Color, alpha: Float = 1f, radius: Dp, stroke: Dp) {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .drawBehind {
                drawRect(
                    color = color,
                    topLeft = Offset(stroke.toPx(), stroke.toPx() + radius.toPx()),
                    size = Size(size.width - 2 * stroke.toPx(), size.height - 2 * (stroke.toPx() + radius.toPx()))
                )
                drawRect(
                    color = color,
                    topLeft = Offset(stroke.toPx() + radius.toPx(), stroke.toPx()),
                    size = Size(size.width - 2 * (stroke.toPx() + radius.toPx()), size.height - 2 * stroke.toPx())
                )
            }
    )
}
