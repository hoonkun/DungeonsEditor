package arctic.ui.composables.atomic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import arctic.ui.unit.dp


@Composable
fun BlurEffectedImage(
    bitmap: ImageBitmap,
    enabled: Boolean = true,
    containerContentAlignment: Alignment = Alignment.TopStart,
    containerModifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    overlays: @Composable BoxScope.() -> Unit = { }
) =
    Box(contentAlignment = containerContentAlignment, modifier = containerModifier) {
        if (enabled) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.05f)
                    .graphicsLayer { renderEffect = BlurEffect(10.dp.value, 10.dp.value) }
                    .then(imageModifier),
                alpha = 0.85f
            )
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .then(imageModifier)
        )
        overlays()
    }
