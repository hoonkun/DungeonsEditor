package arctic.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import arctic.states.Arctic
import arctic.ui.unit.dp
import arctic.ui.composables.atomic.RetroButton
import dungeons.Localizations

@Composable
fun FileOpenFailureOverlay() {
    val exception = Arctic.overlayState.fileLoadFailed

    OverlayBackdrop(exception != null, 0.6f)
    OverlayAnimator(exception) {
        if (it != null) Content(it)
        else SizeMeasureDummy()
    }
}

@Composable
private fun Content(exception: String) {

    val onNeutral = { Arctic.overlayState.fileLoadFailed = null }

    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("file_load_failed_title"),
            description = Localizations.UiText("file_load_failed_description")
        )
        Spacer(modifier = Modifier.height(10.dp))
        OverlayDescriptionText(text = exception)
        Spacer(modifier = Modifier.height(50.dp))
        RetroButton(
            text = Localizations.UiText("close"),
            color = Color(0xffffffff),
            hoverInteraction = "overlay",
            onClick = onNeutral
        )
    }

}