package arctic.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType

object Arctic {
    var pakState: PakState by mutableStateOf(PakState.Uninitialized)
    var editorState: EditorState? by mutableStateOf(null)
    val overlayState: OverlayState = OverlayState()

    val GlobalPointerListener: Map<PointerEventType, SnapshotStateList<AwaitPointerEventScope.(PointerEvent) -> Unit>> = mapOf(
        PointerEventType.Move to mutableStateListOf(),
        PointerEventType.Enter to mutableStateListOf(),
        PointerEventType.Exit to mutableStateListOf(),
        PointerEventType.Press to mutableStateListOf(),
        PointerEventType.Release to mutableStateListOf()
    )

    enum class PakState {
        Uninitialized, Initialized, NotFound;

        val isIndexing get() = this != Initialized
    }
}
