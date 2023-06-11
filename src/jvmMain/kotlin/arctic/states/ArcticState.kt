package arctic.states

import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType

@Stable
class ArcticState {

    var pakState: PakState by mutableStateOf(PakState.Uninitialized)

    var editorState: EditorState? by mutableStateOf(null)

    val overlayState: OverlayState = OverlayState()

    val GlobalPointerListener: MutableMap<PointerEventType, MutableList<AwaitPointerEventScope.(PointerEvent) -> Unit>> = mutableMapOf(
        PointerEventType.Move to mutableListOf(),
        PointerEventType.Enter to mutableListOf(),
        PointerEventType.Exit to mutableListOf(),
        PointerEventType.Press to mutableListOf(),
        PointerEventType.Release to mutableListOf()
    )

    enum class PakState {
        Uninitialized, Initialized, NotFound;
    }

}

val Arctic = ArcticState()
