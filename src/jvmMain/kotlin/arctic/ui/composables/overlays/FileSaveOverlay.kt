package arctic.ui.composables.overlays

import androidx.compose.runtime.Composable
import arctic.states.arctic

@Composable
fun FileSaveOverlay() {
    val enabled = arctic.dialogs.fileSaveDstSelector

    OverlayBackdrop(enabled, 0.6f)
    OverlayAnimator(enabled) { Content() }
}

@Composable
private fun Content() {
    ContentRoot {
        OverlayTitleDescription(
            title = "저장 진행 중!",
            description = "열린 파일 탐색기에서 저장할 위치를 선택해주세요"
        )
    }
}
