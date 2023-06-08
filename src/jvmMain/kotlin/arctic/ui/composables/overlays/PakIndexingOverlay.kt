package arctic.ui.composables.overlays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import dungeons.PakRegistry
import kotlinx.coroutines.delay

@Composable
fun PakIndexingOverlay() {
    val enabled = !PakRegistry.initialized

    OverlayBackdrop(enabled, 0.6f)
    OverlayAnimator(enabled) { Content() }

    LaunchedEffect(true) {
        delay(100)
        PakRegistry.initialize()
    }
}

@Composable
private fun Content() {
    ContentRoot {
        OverlayTitleDescription(
            title = "게임 파일/리소스를 읽는 중입니다",
            description = "다소 시간이 걸릴 수 있으니 조금만 기다려주세요."
        )
    }
}
