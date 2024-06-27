package kiwi.hoonkun.ui.composables.base

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import kotlin.math.absoluteValue
import kotlin.math.ceil


@Composable
fun AutosizeText(
    text: String,
    modifier: Modifier = Modifier,
    acceptableError: Dp = 5.dp,
    maxFontSize: TextUnit = TextUnit.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    contentAlignment: Alignment? = null,
    maxLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    key(text) {
        val alignment: Alignment = contentAlignment ?: when (style.textAlign) {
            TextAlign.Left -> Alignment.TopStart
            TextAlign.Right -> Alignment.TopEnd
            TextAlign.Center -> Alignment.Center
            TextAlign.Justify -> Alignment.TopCenter
            TextAlign.Start -> Alignment.TopStart
            TextAlign.End -> Alignment.TopEnd
            else -> Alignment.TopStart
        }
        BoxWithConstraints(modifier = modifier, contentAlignment = alignment) {
            var shrunkFontSize = if (maxFontSize.isSpecified) maxFontSize else 100.sp

            val calculateIntrinsics = @Composable {
                val mergedStyle = style.merge(TextStyle(fontSize = shrunkFontSize))
                Paragraph(
                    text = text,
                    style = mergedStyle,
                    constraints = Constraints(maxWidth = ceil(LocalDensity.current.run { maxWidth.toPx() }).toInt()),
                    density = LocalDensity.current,
                    fontFamilyResolver = LocalFontFamilyResolver.current,
                    spanStyles = listOf(),
                    placeholders = listOf(),
                    maxLines = maxLines,
                    ellipsis = false
                )
            }

            var intrinsics = calculateIntrinsics()

            val targetWidth = maxWidth - acceptableError / 2f

            check(targetWidth.isFinite || maxFontSize.isSpecified) { "maxFontSize must be specified if the target with isn't finite!" }

            with(LocalDensity.current) {
                if (maxFontSize.isUnspecified || targetWidth < intrinsics.minIntrinsicWidth.toDp()) {
                    while ((targetWidth - intrinsics.minIntrinsicWidth.toDp()).toPx().absoluteValue.toDp() > acceptableError / 2f) {
                        shrunkFontSize *= targetWidth.toPx() / intrinsics.minIntrinsicWidth
                        intrinsics = calculateIntrinsics()
                    }
                }

                while (intrinsics.didExceedMaxLines || maxHeight < intrinsics.height.toDp() || maxWidth < intrinsics.minIntrinsicWidth.toDp()) {
                    shrunkFontSize *= 0.9f
                    intrinsics = calculateIntrinsics()
                }
            }

            if (maxFontSize.isSpecified && shrunkFontSize > maxFontSize) {
                shrunkFontSize = maxFontSize
            }

            Text(
                text = text,
                fontSize = shrunkFontSize,
                style = style,
                onTextLayout = onTextLayout,
                maxLines = maxLines
            )
        }
    }
}