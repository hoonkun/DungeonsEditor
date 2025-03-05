package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kiwi.hoonkun.ui.reusables.minimizableAnimateFloatAsState
import kiwi.hoonkun.ui.reusables.minimizableSpec
import kiwi.hoonkun.ui.reusables.round
import kiwi.hoonkun.ui.units.dp


interface BlurShadowImageDrawCache {
    data object None: BlurShadowImageDrawCache
}

private val DefaultPaint = Paint()

@Composable
fun <T: BlurShadowImageDrawCache>BlurShadowImage(
    bitmap: ImageBitmap,
    enabled: Boolean = true,
    modifier: Modifier,
    clickableModifier: Modifier? = null,
    contentScale: Float = 1f,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    contentPaint: () -> Paint = { DefaultPaint },
    drawCacheFactory: CacheDrawScope.(IntOffset, IntSize) -> T,
    onDrawBehind: DrawScope.(T, IntOffset, IntSize) -> Unit = { _, _, _ -> },
    onDrawFront: DrawScope.(T, IntOffset, IntSize) -> Unit = { _, _, _ -> }
) {
    val blurAlpha by minimizableAnimateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = minimizableSpec { spring() }
    )

    Box(modifier = modifier) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val cache = drawCacheFactory(IntOffset.Zero, size.round())
                    onDrawBehind { onDrawBehind(cache, IntOffset.Zero, size.round()) }
                }
                .padding(contentPadding)
                .blur(10.dp)
                .scale(contentScale * 1.05f)
                .graphicsLayer { alpha = blurAlpha }
        )
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val cache = drawCacheFactory(IntOffset.Zero, size.round())
                    onDrawWithContent {
                        drawIntoCanvas {
                            it.saveLayer(Rect(0f, 0f, size.width, size.height), contentPaint())
                            scale(contentScale) { this@onDrawWithContent.drawContent() }
                            it.restore()
                        }
                        onDrawFront(cache, IntOffset.Zero, size.round())
                    }
                }
                .padding(contentPadding)
        )
        if (clickableModifier !== null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(clickableModifier)
            )
        }
    }
}

@Composable
fun BlurShadowImage(
    bitmap: ImageBitmap,
    enabled: Boolean = true,
    clickableModifier: Modifier? = null,
    modifier: Modifier,
    contentScale: Float = 1f,
    contentPaint: () -> Paint = { DefaultPaint },
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    onDrawBehind: DrawScope.(IntOffset, IntSize) -> Unit = { _, _ -> },
    onDrawFront: DrawScope.(IntOffset, IntSize) -> Unit = { _, _ -> }
) {
    BlurShadowImage(
        bitmap = bitmap,
        enabled = enabled,
        modifier = modifier,
        contentPadding = contentPadding,
        clickableModifier = clickableModifier,
        contentPaint = contentPaint,
        contentScale = contentScale,
        drawCacheFactory = { _, _ -> BlurShadowImageDrawCache.None },
        onDrawBehind = { _, offset, size -> onDrawBehind(offset, size) },
        onDrawFront = { _, offset, size -> onDrawFront(offset, size) }
    )
}
