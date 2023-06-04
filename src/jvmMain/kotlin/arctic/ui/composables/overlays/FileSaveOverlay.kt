package arctic.ui.composables.overlays

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import arctic.states.arctic
import arctic.ui.unit.dp
import arctic.ui.utils.rememberMutableInteractionSource
import arctic.ui.composables.Selector

@Composable
fun FileSaveOverlay() {
    val enabled = arctic.dialogs.fileSaveDstSelector

    OverlayBackdrop(enabled, 0.6f) { arctic.dialogs.fileSaveDstSelector = false }
    OverlayAnimator(enabled) { Content() }
}

@Composable
private fun Content() {
    ContentRoot {
        OverlayTitleDescription(
            title = "저장할 위치를 선택해주세요!",
            description = "아마도 다른 탐색기로 경로를 복사해서 붙혀넣는게 편할 수도 있어요..."
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.size(1050.dp, 640.dp).clickable(rememberMutableInteractionSource(), null) { }) {
            Selector(selectText = "저장", validator = { !it.isDirectory }) {
                arctic.requireStored.save(it); arctic.dialogs.fileSaveDstSelector = false
            }
        }
    }
}
