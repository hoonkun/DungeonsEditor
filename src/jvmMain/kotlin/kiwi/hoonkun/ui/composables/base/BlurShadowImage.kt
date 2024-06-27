package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
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
    contentScale: Float = 1f,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    contentPaint: () -> Paint = { DefaultPaint },
    drawCacheFactory: CacheDrawScope.(IntOffset, IntSize) -> T,
    onDrawBehind: DrawScope.(T, IntOffset, IntSize) -> Unit = { _, _, _ -> },
    onDrawFront: DrawScope.(T, IntOffset, IntSize) -> Unit = { _, _, _ -> }
) {
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current

    val blurAlpha by minimizableAnimateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = minimizableSpec { spring() }
    )

    val paddingLeft = remember { with(density) { contentPadding.calculateLeftPadding(layoutDirection).toPx() } }
    val paddingTop = remember { with(density) { contentPadding.calculateTopPadding().toPx() } }
    val paddingHorizontal = remember { with(density) { paddingLeft + contentPadding.calculateRightPadding(layoutDirection).toPx() } }
    val paddingVertical = remember { with(density) { paddingTop + contentPadding.calculateBottomPadding().toPx() } }

    val calculateDstPlacement: CacheDrawScope.() -> Pair<IntOffset, IntSize> = {
        Offset(paddingLeft, paddingTop).round() to Size(size.width - paddingHorizontal, size.height - paddingVertical).round()
    }

    Box(modifier = modifier) {
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .blur(10.dp)
                .drawWithCache {
                    val (dstOffset, dstSize) = calculateDstPlacement()
                    onDrawBehind {
                        scale(contentScale * 1.05f) {
                            drawImage(
                                image = bitmap,
                                dstOffset = dstOffset,
                                dstSize = dstSize,
                                alpha = blurAlpha,
                            )
                        }
                    }
                },
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val (dstOffset, dstSize) = calculateDstPlacement()
                    val cache = drawCacheFactory(dstOffset, dstSize)
                    onDrawBehind {
                        onDrawBehind(cache, dstOffset, dstSize)
                        drawIntoCanvas {
                            it.saveLayer(Rect(0f, 0f, size.width, size.height), contentPaint())
                            scale(contentScale) {
                                drawImage(bitmap, dstOffset = dstOffset, dstSize = dstSize)
                            }
                            it.restore()
                        }
                        onDrawFront(cache, dstOffset, dstSize)
                    }
                }
        )
    }
}

@Composable
fun BlurShadowImage(
    bitmap: ImageBitmap,
    enabled: Boolean = true,
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
        contentPaint = contentPaint,
        contentScale = contentScale,
        drawCacheFactory = { _, _ -> BlurShadowImageDrawCache.None },
        onDrawBehind = { _, offset, size -> onDrawBehind(offset, size) },
        onDrawFront = { _, offset, size -> onDrawFront(offset, size) }
    )
}
