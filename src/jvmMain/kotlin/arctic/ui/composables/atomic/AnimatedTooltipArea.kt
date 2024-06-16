package arctic.ui.composables.atomic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.Popup
import arctic.states.Arctic
import arctic.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AnimatedTooltipArea(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 500,
    tooltipPlacement: TooltipPlacement = TooltipPlacement.ComponentRect(
        anchor = Alignment.BottomCenter,
        alignment = Alignment.BottomCenter,
        offset = DpOffset(x = 0.dp, y = 10.dp)
    ),
    content: @Composable () -> Unit
) {
    var parentBounds by remember { mutableStateOf(Rect.Zero) }
    var popupPosition by remember { mutableStateOf(Offset.Zero) }
    var isVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var job: Job? by remember { mutableStateOf(null) }

    fun startShowing() {
        job?.cancel()
        job = scope.launch {
            delay(delayMillis.toLong())
            isVisible = true
        }
    }

    fun hide() {
        isVisible = false
    }

    fun hideIfNotHovered(globalPosition: Offset) {
        if (!parentBounds.contains(globalPosition)) {
            hide()
        }
    }

    DisposableEffect(Unit) {
        val listener: AwaitPointerEventScope.(PointerEvent) -> Unit = { hideIfNotHovered(it.position) }

        Arctic.GlobalPointerListener.getValue(PointerEventType.Move).add(listener)
        onDispose { Arctic.GlobalPointerListener.getValue(PointerEventType.Move).remove(listener) }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { parentBounds = it.boundsInWindow() }
            .onPointerEvent(PointerEventType.Enter) { startShowing() }
            .onPointerEvent(PointerEventType.Move) {
                hideIfNotHovered(parentBounds.topLeft + it.position)
            }
            .onPointerEvent(PointerEventType.Exit) {
                hideIfNotHovered(parentBounds.topLeft + it.position)
            }
            .onPointerEvent(PointerEventType.Press) {
                hide()
            }
    ) {
        content()
        Popup(
            popupPositionProvider = tooltipPlacement.positionProvider(Offset.Zero),
            onDismissRequest = { isVisible = false }
        ) {
            AnimatedVisibility(isVisible, enter = fadeIn(), exit = fadeOut(), modifier = Modifier) {
                Box(
                    Modifier.onGloballyPositioned { popupPosition = it.positionInWindow() }
                ) {
                    tooltip()
                }
            }
        }
    }
}

private val PointerEvent.position get() = changes.first().position
