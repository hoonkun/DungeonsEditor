package arctic.ui.composables.overlays

import androidx.compose.runtime.Composable
import arctic.states.Arctic

@Composable
fun InventoryFullOverlay() {
    val enabled = Arctic.overlayState.inventoryFull

    OverlayBackdrop(enabled) { Arctic.overlayState.inventoryFull = false }
    OverlayAnimator(enabled) { Content() }
}

@Composable
private fun Content() {
    ContentRoot {
        OverlayTitleDescription(
            title = "인벤토리가 가득 차서 더 이상 추가할 수 없어요. 먼저 아이템을 삭제하거나 창고로 옮겨보세요!",
            description = "닫으려면 아무 곳이나 누르세요"
        )
    }
}
