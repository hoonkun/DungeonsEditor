package arctic.ui.composables.overlays.extended

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import arctic.ui.unit.dp


fun <T>tween250() = tween<T>(durationMillis = 250)

class OverlayTransitions {
    companion object {
        fun <S>detail(
            slideEnabled: (S, S) -> Boolean = { _, _ -> true },
            sizeTransformDuration: (S, S) -> Int = { _, _ -> 250 }
        ): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
            val slide = slideEnabled(this.initialState, this.targetState)

            var enter = fadeIn(tween250())
            if (slide) enter += slideIn(tween250(), initialOffset = { IntOffset(0, 50.dp.value.toInt()) })

            var exit = fadeOut(tween250())
            if (slide) exit += slideOut(tween250(), targetOffset = { IntOffset(0, -50.dp.value.toInt()) })

            val duration = sizeTransformDuration(this.initialState, this.targetState)
            enter togetherWith exit using SizeTransform(false) { _, _ -> tween(durationMillis = duration) }
        }
    }
}
