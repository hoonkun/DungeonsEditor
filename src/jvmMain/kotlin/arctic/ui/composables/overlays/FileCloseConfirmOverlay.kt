package arctic.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import arctic.states.arctic
import arctic.ui.unit.dp
import arctic.ui.composables.atomic.RetroButton

@Composable
fun FileCloseConfirmOverlay() {
    val enabled = arctic.alerts.closeFile

    OverlayBackdrop(enabled) { arctic.alerts.closeFile = false }
    OverlayAnimator(enabled) { Content() }
}

@Composable
private fun Content() {

    val onNegative = { arctic.alerts.closeFile = false }
    val onPositive = {
        arctic.selection.clearSelection()
        arctic.stored = null
        arctic.alerts.closeFile = false
    }

    ContentRoot {
        OverlayTitleDescription(
            title = "정말 편집을 마치고 파일을 닫으시겠어요?",
            description = "마지막 저장 이후에 만든 변경사항은 저장되지 않아요"
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = "취소",
                color = Color(0xffffffff),
                hoverInteraction = "overlay",
                onClick = onNegative
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = "닫기",
                color = Color(0xffff6e25),
                hoverInteraction = "outline",
                onClick = onPositive
            )
        }
    }
}