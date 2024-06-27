package kiwi.hoonkun.ui.composables.overlays

import MainMenuButtons
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.FileSelector
import kiwi.hoonkun.ui.states.OverlayCloser
import kiwi.hoonkun.ui.units.dp

@Composable
fun PakNotFoundOverlay(
    onSelect: (newPath: String) -> Unit,
    requestClose: OverlayCloser,
    requestExitApp: () -> Unit,
) {
    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("pak_not_found_title"),
            description = Localizations.UiText("pak_not_found_description")
        )
        Spacer(modifier = Modifier.height(52.dp))
        Box(modifier = Modifier.width(1050.dp)) {
            FileSelector(
                buttonText = Localizations.UiText("select"),
                validator = { it.isDirectory && it.listFiles()?.any { file -> file.extension == "pak" } == true },
                onSelect = {
                    onSelect(it.absolutePath)
                    requestClose()
                }
            )
        }
    }
    Box(modifier = Modifier.fillMaxSize().padding(vertical = 40.dp, horizontal = 48.dp)) {
        Row(modifier = Modifier.align(Alignment.BottomStart).scale(1.25f)) {
            MainMenuButtons(description = null, requestExit = requestExitApp)
        }
    }
}