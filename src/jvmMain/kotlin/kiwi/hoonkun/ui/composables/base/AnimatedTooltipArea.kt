package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.rememberComponentRectPositionProvider
import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.LocalAppPointerListeners
import kiwi.hoonkun.ui.units.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AnimatedTooltipArea(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Long = 500,
    tooltipPositionProvider: PopupPositionProvider = rememberComponentRectPositionProvider(
        anchor = Alignment.BottomCenter,
        alignment = Alignment.BottomCenter,
        offset = DpOffset(x = 0.dp, y = 10.dp)
    ),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    val job = remember { mutableRefOf<Job?>(null) }
    val parentBounds = remember { mutableRefOf(Rect.Zero) }

    var isVisible by remember { mutableStateOf(false) }

    val appPointerListeners = LocalAppPointerListeners.current

    fun hideIfNotHovered(globalPosition: Offset) {
        if (parentBounds.value.contains(globalPosition)) return
        job.value?.cancel()
        isVisible = false
    }

    DisposableEffect(Unit) {
        val listener: AwaitPointerEventScope.(PointerEvent) -> Unit = { hideIfNotHovered(it.position) }

        appPointerListeners[PointerEventType.Move] += listener
        onDispose { appPointerListeners[PointerEventType.Move] -= listener }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { parentBounds.value = it.boundsInWindow() }
            .onPointerEvent(PointerEventType.Enter) {
                job.value?.cancel()
                job.value = scope.launch {
                    delay(delayMillis)
                    isVisible = true
                }
            }
            .onPointerEvent(PointerEventType.Move) {
                hideIfNotHovered(parentBounds.value.topLeft + it.position)
            }
            .onPointerEvent(PointerEventType.Exit) {
                hideIfNotHovered(parentBounds.value.topLeft + it.position)
            }
            .onPointerEvent(PointerEventType.Press) {
                isVisible = false
            }
    ) {
        content()
        Popup(
            popupPositionProvider = tooltipPositionProvider,
            onDismissRequest = { isVisible = false }
        ) {
             MinimizableAnimatedVisibility(
                visible = isVisible,
                enter = minimizableEnterTransition { fadeIn() },
                exit = if (ArcticSettings.minimizeAnimations) ExitTransition.None else fadeOut(),
                modifier = Modifier
            ) {
                Box {
                    tooltip()
                }
            }
        }
    }
}

private val PointerEvent.position get() = changes.first().position
