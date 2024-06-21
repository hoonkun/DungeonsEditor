package kiwi.hoonkun.ui.reusables

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.layout

fun Modifier.offsetRelative(
    offset: Offset
): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(placeable.width, placeable.height) {
            placeable.placeRelative(
                x = (placeable.width * offset.x).toInt(),
                y = (placeable.height * offset.y).toInt()
            )
        }
    }


fun Modifier.offsetRelative(
    x: Float = 0f,
    y: Float = 0f,
): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(placeable.width, placeable.height) {
            placeable.placeRelative(
                x = (placeable.width * x).toInt(),
                y = (placeable.height * y).toInt()
            )
        }
    }


private class GrayScaleModifier(private val amount: () -> Float): DrawModifier {
    override fun ContentDrawScope.draw() {
        val saturationMatrix = ColorMatrix().apply { setToSaturation(amount()) }

        val saturationFilter = ColorFilter.colorMatrix(saturationMatrix)
        val paint = Paint().apply {
            colorFilter = saturationFilter
        }
        drawIntoCanvas {
            it.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
            drawContent()
            it.restore()
        }
    }
}

fun Modifier.grayscale(amount: () -> Float = { 0f }) = this.then(GrayScaleModifier(amount))

fun Modifier.grayscale(amount: Float = 0f) = grayscale { amount }

// 접근성을 망칠 것 같은데 다른 방법이 없을까?
@Composable
fun Modifier.consumeClick() = clickable(rememberMutableInteractionSource(), indication = null, onClick = { })
