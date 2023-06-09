package arctic.states

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import dungeons.states.DungeonsJsonState

@Stable
class EditorState {

    val stored: DungeonsJsonState? by mutableStateOf(null)
    val requireStored: DungeonsJsonState by derivedStateOf { stored!! }

    val view: EditorView by mutableStateOf(EditorView.Inventory)

    enum class EditorView {
        Inventory, Storage
    }

}