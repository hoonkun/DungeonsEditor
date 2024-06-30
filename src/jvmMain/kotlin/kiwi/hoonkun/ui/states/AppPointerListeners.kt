package kiwi.hoonkun.ui.states

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

typealias AwaitPointerEventHandler = AwaitPointerEventScope.(PointerEvent) -> Unit

@Stable
class AppPointerListeners {
    private val entries: Map<PointerEventType, SnapshotStateList<AwaitPointerEventHandler>> = mapOf(
        PointerEventType.Move to mutableStateListOf(),
        PointerEventType.Enter to mutableStateListOf(),
        PointerEventType.Exit to mutableStateListOf(),
        PointerEventType.Press to mutableStateListOf(),
        PointerEventType.Release to mutableStateListOf()
    )

    operator fun get(type: PointerEventType) = entries.getValue(type)

    @OptIn(ExperimentalComposeUiApi::class)
    fun onGlobalPointerEvent(): Modifier =
        entries.entries.fold(Modifier.then(Modifier)) { acc, (event, handlers) ->
            acc.onPointerEvent(event) { scope -> handlers.forEach { it(scope) } }
        }
}

val LocalAppPointerListeners = staticCompositionLocalOf { AppPointerListeners() }
