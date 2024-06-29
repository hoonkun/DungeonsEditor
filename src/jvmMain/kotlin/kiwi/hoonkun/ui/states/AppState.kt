package kiwi.hoonkun.ui.states

import androidx.compose.runtime.*
import androidx.compose.ui.window.ApplicationScope
import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.io.DungeonsJsonFile

@Stable
class AppState(
    private val scope: ApplicationScope? = null
) {
    var editorCandidate by mutableStateOf<DungeonsJsonFile.Preview>(DungeonsJsonFile.Preview.None)

    val openedEditors = mutableStateMapOf<String, EditorState>()
    var activeEditorKey by mutableStateOf<String?>(null)

    val activeEditor get() = openedEditors[activeEditorKey]

    val isInEditor get() = activeEditorKey != null

    fun sketchEditor(path: String) {
        if (!openedEditors.containsKey(path)) {
            openedEditors[path] = EditorState.fromPath(path)
        }

        activeEditorKey = path
        editorCandidate = DungeonsJsonFile.Preview.None

        ArcticSettings.updateRecentFiles(path)
    }

    fun eraseEditor() {
        val selectedKey = activeEditorKey
        activeEditorKey = null
        openedEditors.remove(selectedKey)
    }

    fun exitApplication() = scope?.exitApplication()

    object Constants {
        val EntriesWidth = 550.dp
        const val SlidingRatio = 0.2f
    }

}

val LocalAppState = staticCompositionLocalOf { AppState() }