package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.runtime.Composable
import kiwi.hoonkun.resources.Localizations

@Composable
fun InventoryFullOverlay() {
    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations["inventory_full_title"],
            description = Localizations["tap_to_close"]
        )
    }
}