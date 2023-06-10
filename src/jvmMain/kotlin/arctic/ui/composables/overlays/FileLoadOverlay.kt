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
            title = "수정할 파일을 선택해주세요!",
            description = "아마도 다른 탐색기로 경로를 복사해서 붙혀넣는게 편할 수도 있어요..."
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.size(1050.dp, 640.dp).clickable(rememberMutableInteractionSource(), null) { }) {
            Selector(selectText = "열기", validator = { !it.isDirectory && it.isFile }) {
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
