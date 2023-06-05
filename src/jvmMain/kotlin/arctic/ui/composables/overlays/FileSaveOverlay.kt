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
            description = "디렉터리를 선택하면 기존 파일의 이름을 그대로 사용하여 선택한 디렉터리에 저장하고, 그렇지 않을 경우 입력한 경로의 파일에 저장합니다."
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.size(1050.dp, 640.dp).clickable(rememberMutableInteractionSource(), null) { }) {
            Selector(selectText = "저장") {
                arctic.requireStored.save(it); arctic.dialogs.fileSaveDstSelector = false
            }
        }
    }
}
