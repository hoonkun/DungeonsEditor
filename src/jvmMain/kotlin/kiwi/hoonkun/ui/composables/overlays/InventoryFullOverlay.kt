package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.runtime.Composable
import dungeons.Localizations

@Composable
fun InventoryFullOverlay() {
    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("inventory_full_title"),
            description = Localizations.UiText("tap_to_close")
        )
    }
}