package arctic.states

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

@Stable
class ArcticState {

    val pakState: PakState by mutableStateOf(PakState.Uninitialized)

    val editorState: EditorState? by mutableStateOf(null)

    val overlayState: OverlayState = OverlayState()

    enum class PakState {
        Uninitialized, Initialized, NotFound;
    }

}