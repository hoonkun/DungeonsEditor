package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import kiwi.hoonkun.ui.units.dp

@Composable
fun BlurShadowImage(
    bitmap: ImageBitmap,
    contentDescription: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = { }
) {
    Box(
        modifier = modifier
    ) {
        AnimatedVisibility(enabled) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                alpha = 0.85f,
                modifier = Modifier
                    .matchParentSize()
                    .scale(1.05f)
                    .blur(10.dp)
            )
        }
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = Modifier
                .matchParentSize()
        )
        overlay()
    }
}