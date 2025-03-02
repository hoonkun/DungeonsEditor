package kiwi.hoonkun.ui.composables.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.units.dp

@Composable
fun RetroButton(
    color: () -> Color,
    modifier: Modifier = RetroButtonDefaultSizeModifier,
    enabled: Boolean = true,
    hoverInteraction: RetroButtonHoverInteraction,
    disabledColor: Color = Color(0xff666666),
    radius: RetroButtonDpCornerRadius = RetroButtonDpCornerRadius(),
    stroke: Dp = 5.dp,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    contentArrangement: Arrangement.Horizontal = Arrangement.Center,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val source = rememberMutableInteractionSource()
    val hovered by source.collectIsHoveredAsState()
    val pressed by source.collectIsPressedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = contentArrangement,
        modifier = modifier
            .drawWithCache {
                val solidColor = if (enabled) color() else disabledColor
                val path = RetroIndicator(radius)
                onDrawBehind {
                    if (hoverInteraction == RetroButtonHoverInteraction.Outline) {
                        drawPath(path = path, color = solidColor)
                        if (pressed) drawPath(path = path, color = Color.Black.copy(alpha = 0.25f))
                        if (hovered) drawPath(path = path, color = Color.White, style = Stroke(width = stroke.toPx()))
                    } else if (hoverInteraction == RetroButtonHoverInteraction.Overlay) {
                        if (pressed) drawPath(path = path, color = Color.Black.copy(alpha = 0.25f))
                        if (hovered) drawPath(path = path, color = solidColor.copy(alpha = 0.2f))
                    }
                }
            }
            .hoverable(source, enabled)
            .clickable(source, null, role = Role.Button, enabled = enabled, onClick = onClick)
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun RetroButton(
    color: Color,
    modifier: Modifier = RetroButtonDefaultSizeModifier,
    enabled: Boolean = true,
    hoverInteraction: RetroButtonHoverInteraction,
    disabledColor: Color = Color(0xff666666),
    radius: RetroButtonDpCornerRadius = RetroButtonDpCornerRadius(),
    stroke: Dp = 5.dp,
    contentArrangement: Arrangement.Horizontal = Arrangement.Center,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    RetroButton(
        color = { color },
        modifier = modifier,
        enabled = enabled,
        hoverInteraction = hoverInteraction,
        disabledColor = disabledColor,
        radius = radius,
        stroke = stroke,
        contentArrangement = contentArrangement,
        contentPadding = contentPadding,
        onClick = onClick,
        content = content
    )
}

@Composable
fun RetroButton(
    text: String,
    color: Color,
    modifier: Modifier = RetroButtonDefaultSizeModifier,
    enabled: Boolean = true,
    hoverInteraction: RetroButtonHoverInteraction,
    disabledColor: Color = Color(0xff666666),
    textStyle: TextStyle = LocalTextStyle.current,
    radius: RetroButtonDpCornerRadius = RetroButtonDpCornerRadius(),
    stroke: Dp = 5.dp,
    contentArrangement: Arrangement.Horizontal = Arrangement.Center,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    textOverflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
    onClick: () -> Unit
) {
    RetroButton(
        text = text,
        color = { color },
        modifier = modifier,
        enabled = enabled,
        textStyle = textStyle,
        hoverInteraction = hoverInteraction,
        disabledColor = disabledColor,
        radius = radius,
        stroke = stroke,
        contentArrangement = contentArrangement,
        contentPadding = contentPadding,
        textOverflow = textOverflow,
        maxLines = maxLines,
        textPadding = textPadding,
        onClick = onClick
    )
}

@Composable
fun RetroButton(
    text: String,
    color: () -> Color,
    modifier: Modifier = RetroButtonDefaultSizeModifier,
    enabled: Boolean = true,
    hoverInteraction: RetroButtonHoverInteraction,
    disabledColor: Color = Color(0xff666666),
    textStyle: TextStyle = LocalTextStyle.current,
    radius: RetroButtonDpCornerRadius = RetroButtonDpCornerRadius(),
    stroke: Dp = 5.dp,
    contentArrangement: Arrangement.Horizontal = Arrangement.Center,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    textOverflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
    onClick: () -> Unit
) {
    RetroButton(
        color = color,
        modifier = modifier,
        enabled = enabled,
        hoverInteraction = hoverInteraction,
        disabledColor = disabledColor,
        radius = radius,
        stroke = stroke,
        contentArrangement = contentArrangement,
        contentPadding = contentPadding,
        onClick = onClick
    ) {
        Text(
            text = text,
            style = textStyle,
            overflow = textOverflow,
            maxLines = maxLines,
            modifier = Modifier.padding(textPadding),
        )
    }
}

enum class RetroButtonHoverInteraction { Outline, Overlay }

val RetroButtonDefaultSizeModifier get() = Modifier.size(190.dp, 55.dp)

data class RetroButtonDpCornerRadius(
    val topStart: Dp = 8.dp,
    val topEnd: Dp = 8.dp,
    val bottomStart: Dp = 8.dp,
    val bottomEnd: Dp = 8.dp
) {
    fun toPxRadius(density: Density) = with (density) {
        RetroButtonCornerRadius(
            topStart.toPx(),
            topEnd.toPx(),
            bottomStart.toPx(),
            bottomEnd.toPx()
        )
    }
}

fun RetroButtonDpCornerRadius(all: Dp) = RetroButtonDpCornerRadius(all, all, all, all)

data class RetroButtonCornerRadius(
    val topStart: Float = 0f,
    val topEnd: Float = 0f,
    val bottomStart: Float = 0f,
    val bottomEnd: Float = 0f
)

fun CacheDrawScope.RetroIndicator(dpRadius: RetroButtonDpCornerRadius = RetroButtonDpCornerRadius()): Path =
    Path().apply {
        val radius = dpRadius.toPxRadius(this@RetroIndicator)
        moveTo(radius.topStart, 0f)
        lineTo(size.width - radius.topEnd, 0f)
        lineTo(size.width - radius.topEnd, radius.topEnd)
        lineTo(size.width, radius.topEnd)
        lineTo(size.width, size.height - radius.bottomEnd)
        lineTo(size.width - radius.bottomEnd, size.height - radius.bottomEnd)
        lineTo(size.width - radius.bottomEnd, size.height)
        lineTo(radius.bottomStart, size.height)
        lineTo(radius.bottomStart, size.height - radius.bottomStart)
        lineTo(0f, size.height - radius.bottomStart)
        lineTo(0f, radius.topStart)
        lineTo(radius.topStart, radius.topStart)
        close()
    }

