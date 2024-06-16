package arctic.ui.composables.overlays.extended

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import arctic.ui.unit.dp

fun <T>defaultTween() = tween<T>(250)

fun defaultFadeIn() = fadeIn(defaultTween())
fun defaultFadeOut() = fadeOut(defaultTween())

fun defaultSlideIn(initialOffset: (fullSize: IntSize) -> IntOffset) = slideIn(defaultTween(), initialOffset)
fun defaultSlideOut(targetOffset: (fullSize: IntSize) -> IntOffset) = slideOut(defaultTween(), targetOffset)

object OverlayTransitions {
    fun <S>detail(
        slideEnabled: (S, S) -> Boolean = { _, _ -> true },
        sizeTransformDuration: (S, S) -> Int = { _, _ -> 250 }
    ): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        val slide = slideEnabled(this.initialState, this.targetState)

        var enter = defaultFadeIn()
        if (slide) enter += defaultSlideIn { IntOffset(0, 50.dp.value.toInt()) }

        var exit = defaultFadeOut()
        if (slide) exit += defaultSlideOut { IntOffset(0, -50.dp.value.toInt()) }

        val duration = sizeTransformDuration(this.initialState, this.targetState)
        enter togetherWith exit using SizeTransform(false) { _, _ -> tween(durationMillis = duration) }
    }
}
