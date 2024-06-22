package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.units.dp

@Composable
fun ItemDuplicateLocationConfirmOverlay(
    editor: EditorState,
    target: Item,
    requestClose: () -> Unit
) {
    val onClose = { requestClose() }

    val onOriginalSelected = {
        target.parent.addItem(editor, target.copy(), target)
        onClose()
    }

    val onHereSelected = {
        target.parent.addItem(editor, target.copy())
        onClose()
    }

    val where = target.where ?: editor.view

    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("inventory_duplicate_title", where.localizedName),
            description = Localizations.UiText("inventory_duplicate_description", editor.view.localizedName)
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = Localizations.UiText("inventory_duplicate_button_source"),
                color = Color(0xff3f8e4f),
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = onOriginalSelected,
                modifier = Modifier.size(225.dp, 65.dp)
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = Localizations.UiText("cancel"),
                color = Color(0xffffffff),
                hoverInteraction = RetroButtonHoverInteraction.Overlay,
                onClick = onClose,
                modifier = Modifier.size(225.dp, 65.dp)
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = Localizations.UiText("inventory_duplicate_button_here"),
                color = Color(0xff3f8e4f),
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = onHereSelected,
                modifier = Modifier.size(225.dp, 65.dp)
            )
        }
    }
}
