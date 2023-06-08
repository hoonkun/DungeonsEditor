package arctic.ui.composables.overlays

import LocalData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import arctic.states.arctic
import arctic.ui.composables.Selector
import arctic.ui.unit.dp
import arctic.ui.utils.rememberMutableInteractionSource

@Composable
fun PakNotFoundOverlay() {
    OverlayBackdrop(arctic.pakNotFound)
    OverlayAnimator(arctic.pakNotFound) { Content() }
}

@Composable
private fun Content() {
    ContentRoot {
        OverlayTitleDescription(
            title = "게임 데이터를 찾을 수 없습니다",
            description = "예측 가능한 위치에서 게임파일을 찾지 못했습니다.\n게임 내 리소스 파일(.pak) 디렉터리의 경로를 입력해주세요."
        )
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.size(1050.dp, 640.dp).clickable(rememberMutableInteractionSource(), null) { }) {
            Selector(
                selectText = "선택",
                validator = {
                    it.isDirectory && it.listFiles()?.any { file -> file.extension == "pak" } == true
                }
            ) {
                arctic.pakNotFound = false
                arctic.initialized = false

                LocalData.customPakLocation = it.absolutePath
                LocalData.save()
            }
        }
    }
}