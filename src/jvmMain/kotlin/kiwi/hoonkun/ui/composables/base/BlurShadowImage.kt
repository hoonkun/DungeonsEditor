package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import kiwi.hoonkun.ui.reusables.getValue
import kiwi.hoonkun.ui.reusables.mutableRefOf
import kiwi.hoonkun.ui.reusables.round
import kiwi.hoonkun.ui.reusables.setValue
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
    var blurred by remember(bitmap) { mutableRefOf<ImageBitmap?>(null) }

    val blurAlpha by animateFloatAsState(if (enabled) 1f else 0f)

    val paddingLeft = remember { with(density) { contentPadding.calculateLeftPadding(layoutDirection).toPx() } }
    val paddingTop = remember { with(density) { contentPadding.calculateTopPadding().toPx() } }
    val paddingHorizontal = remember { with(density) { paddingLeft + contentPadding.calculateRightPadding(layoutDirection).toPx() } }
    val paddingVertical = remember { with(density) { paddingTop + contentPadding.calculateBottomPadding().toPx() } }

    Spacer(
        modifier = modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                blurred = renderComposeScene(
                    width = placeable.width,
                    height = placeable.height,
                    density = density,
                    content = {
                        Canvas(
                            modifier = Modifier
                                .size(placeable.width.toDp(), placeable.height.toDp())
                                .blur(10.dp),
                        ) {
                            drawImage(bitmap, dstSize = size.round())
                        }
                    }
                ).toComposeImageBitmap()

                layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
            }
            .drawWithCache {
                val dstOffset = Offset(paddingLeft, paddingTop).round()
                val dstSize = Size(size.width - paddingHorizontal, size.height - paddingVertical).round()

                val cache = drawCacheFactory(dstOffset, dstSize)
                onDrawBehind {
                    onDrawBehind(cache, dstOffset, dstSize)
                    drawIntoCanvas {
                        it.saveLayer(Rect(0f, 0f, size.width, size.height), contentPaint())
                        scale(contentScale * 1.05f) {
                            blurred?.let { rendered ->
                                drawImage(
                                    image = rendered,
                                    dstOffset = dstOffset,
                                    dstSize = dstSize,
                                    alpha = blurAlpha,
                                )
                            }
                        }
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
