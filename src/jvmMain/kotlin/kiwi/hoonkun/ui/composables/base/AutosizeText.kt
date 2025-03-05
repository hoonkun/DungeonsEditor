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
import androidx.compose.ui.text.AnnotatedString
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
        AutosizeText(
            text = AnnotatedString(text),
            modifier = modifier,
            acceptableError = acceptableError,
            maxFontSize = maxFontSize,
            style = style,
            contentAlignment = contentAlignment,
            maxLines = maxLines,
            onTextLayout = onTextLayout,
        )
    }
}

@Composable
fun AutosizeText(
    text: AnnotatedString,
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
                    text = text.text,
                    style = mergedStyle,
                    constraints = Constraints(maxWidth = ceil(LocalDensity.current.run { maxWidth.toPx() }).toInt()),
                    density = LocalDensity.current,
                    fontFamilyResolver = LocalFontFamilyResolver.current,
                    spanStyles = text.spanStyles,
                    placeholders = listOf(),
                    maxLines = maxLines,
                )
            }

            var intrinsics = calculateIntrinsics()

            with(LocalDensity.current) {
                val targetWidth = (maxWidth - acceptableError / 2f).toPx()
                val acceptableErrorPx = acceptableError.toPx()

                check(maxWidth.isFinite || maxFontSize.isSpecified) { "maxFontSize must be specified if the target with isn't finite!" }

                if (maxFontSize.isUnspecified || targetWidth < intrinsics.minIntrinsicWidth) {
                    while ((targetWidth - intrinsics.minIntrinsicWidth).absoluteValue > acceptableErrorPx / 2f) {
                        shrunkFontSize *= targetWidth / intrinsics.minIntrinsicWidth
                        intrinsics = calculateIntrinsics()
                    }
                }

                while (intrinsics.didExceedMaxLines || maxHeight.toPx() < intrinsics.height || maxWidth.toPx() < intrinsics.minIntrinsicWidth) {
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