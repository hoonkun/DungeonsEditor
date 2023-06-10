package arctic.states

import androidx.compose.runtime.*

@Stable
class ArcticState {

    var pakState: PakState by mutableStateOf(PakState.Uninitialized)

    var editorState: EditorState? by mutableStateOf(null)

    val overlayState: OverlayState = OverlayState()

    enum class PakState {
        Uninitialized, Initialized, NotFound;
    }

}

val Arctic = ArcticState()
