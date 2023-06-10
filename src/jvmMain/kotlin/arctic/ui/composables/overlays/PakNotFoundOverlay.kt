package arctic.ui.composables.overlays

import LocalData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import arctic.states.Arctic
import arctic.states.ArcticState
import arctic.ui.composables.Selector
import arctic.ui.unit.dp
import arctic.ui.utils.rememberMutableInteractionSource
import dungeons.Localizations

@Composable
fun PakNotFoundOverlay() {
    OverlayBackdrop(Arctic.pakState == ArcticState.PakState.NotFound)
    OverlayAnimator(Arctic.pakState == ArcticState.PakState.NotFound) { Content() }
}

@Composable
private fun Content() {
    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("pak_not_found_title"),
            description = Localizations.UiText("pak_not_found_description")
        )
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.size(1050.dp, 640.dp).clickable(rememberMutableInteractionSource(), null) { }) {
            Selector(
                selectText = Localizations.UiText("select"),
                validator = {
                    it.isDirectory && it.listFiles()?.any { file -> file.extension == "pak" } == true
                }
            ) {
                Arctic.pakState = ArcticState.PakState.Uninitialized

                LocalData.customPakLocation = it.absolutePath
                LocalData.save()
            }
        }
    }
}