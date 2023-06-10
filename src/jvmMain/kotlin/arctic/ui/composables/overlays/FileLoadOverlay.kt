package arctic.ui.composables.overlays

import LocalData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import arctic.states.Arctic
import arctic.states.EditorState
import arctic.ui.unit.dp
import arctic.ui.utils.rememberMutableInteractionSource
import arctic.ui.composables.Selector
import dungeons.Localizations
import dungeons.readDungeonsJson
import dungeons.states.DungeonsJsonState
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

@Composable
fun FileLoadOverlay() {
    val enabled = Arctic.overlayState.fileLoadSrcSelector

    OverlayBackdrop(enabled, 0.6f) { Arctic.overlayState.fileLoadSrcSelector = false }
    OverlayAnimator(enabled) { Content() }
}

@Composable
private fun Content() {
    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("file_load_src_title"),
            description = Localizations.UiText("file_load_src_description")
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.size(1050.dp, 640.dp).clickable(rememberMutableInteractionSource(), null) { }) {
            Selector(selectText = Localizations.UiText("open"), validator = { !it.isDirectory && it.isFile }) {
                try {
                    Arctic.editorState = EditorState(DungeonsJsonState(it.readDungeonsJson(), it))

                    LocalData.updateRecentFiles(Path(it.absolutePath).normalize().absolutePathString())
                } catch (e: Exception) {
                    Arctic.overlayState.fileLoadFailed = e.message
                } finally {
                    Arctic.overlayState.fileLoadSrcSelector = false
                }
            }
        }
    }
}
