package kiwi.hoonkun.ui.composables.base.overlays

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dungeons.Localizations
import kiwi.hoonkun.ui.composables.base.FileSelector
import kiwi.hoonkun.ui.units.dp

@Composable
fun PakNotFoundOverlay(
    onSelect: (newPath: String) -> Unit
) {
    arctic.ui.composables.overlays.ContentRoot {
        arctic.ui.composables.overlays.OverlayTitleDescription(
            title = Localizations.UiText("pak_not_found_title"),
            description = Localizations.UiText("pak_not_found_description")
        )
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.size(1050.dp, 640.dp)) {
            FileSelector(
                selectText = Localizations.UiText("select"),
                validator = { it.isDirectory && it.listFiles()?.any { file -> file.extension == "pak" } == true },
                onSelect = { onSelect(it.absolutePath) }
            )
        }
    }
}