package arctic.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import arctic.states.arctic
import arctic.ui.unit.dp
import arctic.ui.composables.atomic.RetroButton

@Composable
fun FileOpenFailureOverlay() {
    val exception = arctic.alerts.fileLoadFailed

    OverlayBackdrop(exception != null, 0.6f)

    OverlayAnimator(exception) {
        if (it != null) Content(it)
        else SizeMeasureDummy()
    }
}

@Composable
private fun Content(exception: String) {

    val onNeutral = { arctic.alerts.fileLoadFailed = null }

    ContentRoot {
        OverlayTitleDescription(
            title = "파일 로드에 실패했어요",
            description = "잘못된 파일이거나, 개발자가 이상한 짓을 해서 그럴 수도 있어요."
        )
        Spacer(modifier = Modifier.height(10.dp))
        OverlayDescriptionText(text = exception)
        Spacer(modifier = Modifier.height(50.dp))
        RetroButton(
            text = "닫기",
            color = Color(0xffffffff),
            hoverInteraction = "overlay",
            onClick = onNeutral
        )
    }

}