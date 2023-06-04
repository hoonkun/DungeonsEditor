package arctic.ui.composables.overlays.extended

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import arctic.ui.unit.dp


@Composable
fun ComplicatedOverlays() {
    ItemCreationOverlay()
    ItemEditionOverlay()

    EnchantmentModificationOverlay()
    ArmorPropertyModificationOverlay()
}

fun <T>tween250() = tween<T>(durationMillis = 250)

class OverlayTransitions {
    companion object {

        @OptIn(ExperimentalAnimationApi::class)
        fun <S>collection(reversed: Boolean = false): AnimatedContentScope<S>.() -> ContentTransform = {
            val enter = fadeIn(tween250()) + slideIn(tween250(), initialOffset = { IntOffset((if (reversed) 1 else -1) * 70.dp.value.toInt(), 0) })
            val exit = fadeOut(tween250())
            enter with exit
        }

        @OptIn(ExperimentalAnimationApi::class)
        fun <S>detail(
            slideEnabled: (S, S) -> Boolean = { _, _ -> true },
            sizeTransformDuration: (S, S) -> Int = { _, _ -> 250 }
        ): AnimatedContentScope<S>.() -> ContentTransform = {
            val slide = slideEnabled(this.initialState, this.targetState)

            var enter = fadeIn(tween250())
            if (slide) enter += slideIn(tween250(), initialOffset = { IntOffset(0, 50.dp.value.toInt()) })

            var exit = fadeOut(tween250())
            if (slide) exit += slideOut(tween250(), targetOffset = { IntOffset(0, -50.dp.value.toInt()) })

            val duration = sizeTransformDuration(this.initialState, this.targetState)
            enter with exit using SizeTransform(false) { _, _ -> tween(durationMillis = duration) }
        }

        // TODO: 이거 쓰지 말고 그냥 collection 을 썼을 때는 어떤지 확인할 것. 아마 똑같아야하는데.
        @OptIn(ExperimentalAnimationApi::class)
        fun <S>largeCollectionTransition(scaleEnabled: Boolean): AnimatedContentScope<S>.() -> ContentTransform = {
            val enter = fadeIn(tween250()) + slideIn(tween250(), initialOffset = { IntOffset(- 70.dp.value.toInt(), 0) })
            var exit = fadeOut(tween250())
            if (scaleEnabled) exit += scaleOut(tween250(), targetScale = 0.9f)
            enter with exit
        }

    }
}
