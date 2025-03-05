package kiwi.hoonkun.ui.composables.overlays.tower

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.composables.overlays.OverlayRoot
import kiwi.hoonkun.ui.composables.overlays.OverlayTitleDescription
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp

@Composable
fun TowerModificationWarningOverlay(
    onConfirm: () -> Unit,
    requestClose: () -> Unit
) {
    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations["tower_edit_warning_title"],
            description = Localizations["tower_edit_warning_description"]
        )

        WarningContainer {
            WarningRed(Localizations["tower_edit_desc_1"])
            WarningDescription(Localizations["tower_edit_desc_2"])
            WarningWhite(Localizations["tower_edit_desc_3"])
            WarningDescription(Localizations["tower_edit_desc_4"])
            Spacer(modifier = Modifier.height(0.dp))
            WarningTurn(Localizations["tower_edit_desc_5"])
            Spacer(modifier = Modifier.height(0.dp))
            WarningRed(Localizations["tower_edit_desc_6"])
            WarningDescription(Localizations["tower_edit_desc_7"])
            WarningRed(Localizations["tower_edit_desc_8"])
            WarningDescription(Localizations["tower_edit_desc_9"])
            WarningWhite(Localizations["tower_edit_desc_10"])
            WarningDescription(Localizations["tower_edit_desc_11"])
            WarningWhite(Localizations["tower_edit_desc_12"])
            WarningDescription(Localizations["tower_edit_desc_13"])
            Spacer(modifier = Modifier.height(0.dp))
            WarningTurn(Localizations["tower_edit_desc_14"], color = Color(0xffffc14f))
        }
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = Localizations["tower_edit_back"],
                color = Color(0xffffffff),
                hoverInteraction = RetroButtonHoverInteraction.Overlay,
                onClick = requestClose
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = Localizations["tower_edit_confirm"],
                color = Color(0xffff6e25),
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = onConfirm
            )
        }
    }
}

@Composable
fun WarningContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 64.dp).width(900.dp)
    ) {
        content()
    }
}

@Composable
fun WarningWhite(text: String) {
    Text(text = text, color = Color.White, fontSize = 22.sp)
}

@Composable
fun WarningRed(text: String) {
    Text(text = text, color = Color(0xffff6e25), fontWeight = FontWeight.Bold, fontSize = 22.sp)
}

@Composable
fun WarningDescription(text: String) {
    Text(text = text, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 16.dp))
}

@Composable
fun WarningTurn(text: String, color: Color = Color.White.copy(alpha = 0.5f)) {
    Text(
        text = text,
        color = color,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp),
    )
}
