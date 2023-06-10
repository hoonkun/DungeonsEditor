package arctic.ui.composables.overlays

import androidx.compose.runtime.Composable
import arctic.states.Arctic
import dungeons.Localizations

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
            title = Localizations.UiText("inventory_full_title"),
            description = Localizations.UiText("tap_to_close")
        )
    }
}
