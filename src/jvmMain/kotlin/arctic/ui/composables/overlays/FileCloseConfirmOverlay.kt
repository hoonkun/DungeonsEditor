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
fun FileCloseConfirmOverlay() {
    val enabled = Arctic.overlayState.fileClose

    OverlayBackdrop(enabled) { Arctic.overlayState.fileClose = false }
    OverlayAnimator(enabled) { Content() }
}

@Composable
private fun Content() {

    val onNegative = { Arctic.overlayState.fileClose = false }
    val onPositive = {
        Arctic.editorState = null
        Arctic.overlayState.fileClose = false
    }

    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("close_file_title"),
            description = Localizations.UiText("close_file_description")
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = Localizations.UiText("cancel"),
                color = Color(0xffffffff),
                hoverInteraction = "overlay",
                onClick = onNegative
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = Localizations.UiText("close"),
                color = Color(0xffff6e25),
                hoverInteraction = "outline",
                onClick = onPositive
            )
        }
    }
}