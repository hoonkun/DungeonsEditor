package kiwi.hoonkun.ui.states

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
        canBeDismissed: Boolean = true,
        backdropOptions: Overlay.BackdropOptions = Overlay.BackdropOptions(),
        composable: @Composable BoxScope.(id: String) -> Unit
    ) {
        make(Overlay(backdropOptions = backdropOptions, canBeDismissed = canBeDismissed, composable = composable))
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

    @Composable
    fun Stack(scope: BoxScope) {
        for (item in stack) {
            key(item.id) { scope.Item(item) }
        }
    }

    @Composable
    fun BoxScope.Item(item: Overlay) {
        Backdrop(item)
        Content(item)

        LaunchedEffect(item.visible) {
            if (item.visible) return@LaunchedEffect

            delay(500)
            stack.removeIf { it.id == item.id }
        }
    }

    @Composable
    private fun BoxScope.Content(item: Overlay) {
        AnimatedVisibility(
            visible = item.visible,
            enter = defaultFadeIn() + scaleIn(initialScale = 1.1f),
            exit = defaultFadeOut() + scaleOut(targetScale = 1.1f)
        ) {
            item.composable(this@Content, item.id)
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
                        onClick = item.backdropOptions.onClick
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
    val composable: @Composable BoxScope.(id: String) -> Unit,
) {
    var state by mutableStateOf(State.Idle)
    val visible: Boolean get() = state == State.Idle

    enum class State {
        Idle, Exiting
    }

    data class BackdropOptions(
        val alpha: Float = 0.376f,
        val onClick: () -> Unit = { }
    )
}

@Composable
fun rememberOverlayState() = remember { OverlayState() }
