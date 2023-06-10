package arctic.ui.composables.overlays

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

@Composable
fun FileSaveOverlay(editor: EditorState) {
    val enabled = Arctic.overlayState.fileSaveDstSelector

    OverlayBackdrop(enabled, 0.6f) { Arctic.overlayState.fileSaveDstSelector = false }
    OverlayAnimator(enabled) { Content(editor) }
}

@Composable
private fun Content(editor: EditorState) {
    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("file_save_title"),
            description = Localizations.UiText("file_save_description")
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.size(1050.dp, 640.dp).clickable(rememberMutableInteractionSource(), null) { }) {
            Selector {
                editor.stored.save(it)
                Arctic.overlayState.fileSaveDstSelector = false
            }
        }
    }
}
