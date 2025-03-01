package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.units.dp

@Composable
fun ExitApplicationConfirmOverlay(
    description: String? = Localizations["exit_application_description"],
    onConfirm: () -> Unit,
    requestClose: () -> Unit
) {
    val onNegative = { requestClose() }
    val onPositive = {
        onConfirm()
        requestClose()
    }

    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations["exit_application_title"],
            description = description
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = Localizations["cancel"],
                color = Color(0xffffffff),
                hoverInteraction = RetroButtonHoverInteraction.Overlay,
                onClick = onNegative
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = Localizations["exit"],
                color = Color(0xffff6e25),
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = onPositive
            )
        }
    }
}