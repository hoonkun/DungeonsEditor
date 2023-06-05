package arctic.ui.composables.atomic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import arctic.ui.unit.dp


@Composable
fun BlurEffectedImage(
    bitmap: ImageBitmap,
    alpha: Float = 1.0f,
    scale: Float = 1.0f,
    enabled: Boolean = true,
    containerContentAlignment: Alignment = Alignment.TopStart,
    containerModifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    overlays: @Composable BoxScope.() -> Unit = { }
) =
    Box(contentAlignment = containerContentAlignment, modifier = containerModifier) {
        if (enabled) Image(bitmap, null, modifier = Modifier.fillMaxSize().scale(scale + 0.05f).blur(10.dp).then(imageModifier), alpha = 0.85f * alpha)
        Image(bitmap, null, alpha = alpha, modifier = Modifier.fillMaxSize().scale(scale).then(imageModifier))
        overlays()
    }
