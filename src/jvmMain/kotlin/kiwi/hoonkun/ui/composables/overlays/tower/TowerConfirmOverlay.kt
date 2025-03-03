package kiwi.hoonkun.ui.composables.overlays.tower

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
import kiwi.hoonkun.ui.composables.overlays.OverlayRoot
import kiwi.hoonkun.ui.composables.overlays.OverlayTitleDescription
import kiwi.hoonkun.ui.units.dp

@Composable
fun TowerConfirmOverlay(
    title: String,
    description: String,
    confirmLabel: String,
    requestClose: () -> Unit,
    onConfirm: () -> Unit
) {
    val onNegative = { requestClose() }
    val onPositive = {
        onConfirm()
        requestClose()
    }

    OverlayRoot {
        OverlayTitleDescription(
            title = title,
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
                text = confirmLabel,
                color = Color(0xffff6e25),
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = onPositive
            )
        }
    }
}