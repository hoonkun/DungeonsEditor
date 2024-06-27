package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.runtime.Composable
import kiwi.hoonkun.resources.Localizations

@Composable
fun InventoryFullOverlay() {
    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("inventory_full_title"),
            description = Localizations.UiText("tap_to_close")
        )
    }
}