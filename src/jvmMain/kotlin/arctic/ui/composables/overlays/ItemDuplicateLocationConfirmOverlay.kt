package arctic.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import arctic.states.Arctic
import arctic.states.EditorState
import arctic.ui.unit.dp
import arctic.ui.composables.atomic.RetroButton
import dungeons.Localizations
import dungeons.states.Item
import dungeons.states.extensions.addItem

@Composable
fun ItemDuplicateLocationConfirmOverlay(editor: EditorState) {
    val target = Arctic.overlayState.itemDuplication

    OverlayBackdrop(target != null) { Arctic.overlayState.itemDuplication = null }
    OverlayAnimator(target to target?.where) { (target, where) ->
        if (target != null && where != null) Content(editor, target, where)
        else SizeMeasureDummy()
    }
}

@Composable
private fun Content(editor: EditorState, target: Item, where: EditorState.EditorView) {

    val onClose = { Arctic.overlayState.itemDuplication = null }

    val onOriginalSelected = {
        target.parent.addItem(editor, target.copy(), target)
        onClose()
    }

    val onHereSelected = {
        target.parent.addItem(editor, target.copy())
        onClose()
    }

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
                hoverInteraction = "outline",
                onClick = onOriginalSelected
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = Localizations.UiText("cancel"),
                color = Color(0xffffffff),
                hoverInteraction = "overlay",
                onClick = onClose
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = Localizations.UiText("inventory_duplicate_button_here"),
                color = Color(0xff3f8e4f),
                hoverInteraction = "outline",
                onClick = onHereSelected
            )
        }
    }
}
