package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.runtime.Composable
import kiwi.hoonkun.resources.Localizations

@Composable
fun FileSaveCompleteOverlay() {
    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations["save_complete"],
            description = Localizations["tap_to_close"]
        )
    }
}