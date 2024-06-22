package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.runtime.Composable
import kiwi.hoonkun.resources.Localizations

@Composable
fun FileSaveCompleteOverlay() {
    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("save_complete"),
            description = Localizations.UiText("tap_to_close")
        )
    }
}