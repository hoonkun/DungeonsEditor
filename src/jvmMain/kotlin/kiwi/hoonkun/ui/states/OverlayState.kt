package kiwi.hoonkun.ui.states

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kotlinx.coroutines.delay
import java.util.*

@Stable
class OverlayState {
    private val stack = mutableStateListOf<Overlay>()

    fun make(target: Overlay) = target.also { stack.add(it) }.id
    fun make(
        backdropOptions: Overlay.BackdropOptions = Overlay.BackdropOptions(),
        canBeDismissed: Boolean = true,
        enter: EnterTransition = defaultFadeIn() + scaleIn(initialScale = 1.1f),
        exit: ExitTransition = defaultFadeOut() + scaleOut(targetScale = 1.1f),
        content: @Composable AnimatedVisibilityScope.(id: String) -> Unit
    ) {
        val newOverlay = Overlay(
            backdropOptions = backdropOptions,
            canBeDismissed = canBeDismissed,
            enter = enter,
            exit = exit,
            content = content
        )
        make(newOverlay)
    }
    fun destroy(id: String) {
        stack.first { it.id == id }.state = Overlay.State.Exiting
    }
    fun pop(): Boolean {
        val last = stack.lastOrNull { it.state != Overlay.State.Exiting } ?: return false
        if (!last.canBeDismissed) return false
        last.state = Overlay.State.Exiting
        return true
    }
    fun any(): Boolean = stack.any { it.state == Overlay.State.Idle }

    @Composable
    fun Stack() {
        for (item in stack) {
            key(item.id) { Item(item) }
        }
    }

    @Composable
    fun Item(item: Overlay) {
        Backdrop(item)
        Content(item)

        LaunchedEffect(Unit) { item.state = Overlay.State.Idle }

        LaunchedEffect(item.visible) {
            if (item.visible) return@LaunchedEffect

            delay(500)
            stack.removeIf { it.id == item.id }
        }
    }

    @Composable
    private fun Content(item: Overlay) {
        val isLastItem by remember { derivedStateOf { stack.lastOrNull { it.state == Overlay.State.Idle } != item } }
        val blur by animateFloatAsState(if (isLastItem) 50f else 0f)

        AnimatedVisibility(
            visible = item.visible,
            enter = item.enter,
            exit = item.exit,
            modifier = Modifier.graphicsLayer { renderEffect = if (blur == 0f) null else BlurEffect(blur, blur) }
        ) {
            item.content(this, item.id)
        }
    }

    @Composable
    private fun Backdrop(item: Overlay) {
        AnimatedVisibility(
            visible = item.visible,
            enter = defaultFadeIn(),
            exit = defaultFadeOut(),
            label = "BackdropOf(${item.id})"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(item.backdropOptions.alpha)
                    .background(Color(0xff000000))
                    .clickable(
                        interactionSource = rememberMutableInteractionSource(),
                        indication = null,
                        onClick = { item.backdropOptions.onClick(this@OverlayState, item) }
                    )
            )
        }
    }
}

@Stable
data class Overlay(
    val id: String = UUID.randomUUID().toString(),
    val backdropOptions: BackdropOptions = BackdropOptions(),
    val canBeDismissed: Boolean = true,
    val enter: EnterTransition = defaultFadeIn() + scaleIn(initialScale = 1.1f),
    val exit: ExitTransition = defaultFadeOut() + scaleOut(targetScale = 1.1f),
    val content: @Composable AnimatedVisibilityScope.(id: String) -> Unit,
) {
    var state by mutableStateOf(State.Initial)
    val visible: Boolean get() = state == State.Idle

    enum class State {
        Initial, Idle, Exiting
    }

    data class BackdropOptions(
        val alpha: Float = 0.376f,
        val onClick: OverlayState.(Overlay) -> Unit = { if (it.canBeDismissed) destroy(it.id) }
    )
}

val LocalOverlayState = staticCompositionLocalOf<OverlayState> { throw RuntimeException("unreachable") }

@Composable
fun rememberOverlayState() = remember { OverlayState() }
