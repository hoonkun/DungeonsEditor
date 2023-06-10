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
import dungeons.states.extensions.deleteItem

@Composable
fun ItemDeletionConfirmOverlay(editor: EditorState) {
    val target = Arctic.overlayState.itemDeletion

    OverlayBackdrop(target != null) { Arctic.overlayState.itemDeletion = null }
    OverlayAnimator(target to target?.where) { (target, where) ->
        if (target != null && where != null) Content(editor, target, where)
        else SizeMeasureDummy()
    }
}

@Composable
private fun Content(editor: EditorState, target: Item, where: EditorState.EditorView) {

    val onNegative = { Arctic.overlayState.itemDeletion = null }
    val onPositive = {
        target.parent.deleteItem(editor, target)
        Arctic.overlayState.itemDeletion = null
    }

    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("inventory_delete_title", if (where != editor.view) Localizations.UiText("inventory_delete_title_arg", where.localizedName) else ""),
            description = Localizations.UiText("inventory_delete_description")
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = Localizations.UiText("cancel"),
                color = Color(0xffffffff),
                hoverInteraction = "overlay",
                onClick = onNegative
            )
            Spacer(modifier = Modifier.width(150.dp))
            RetroButton(
                text = Localizations.UiText("delete"),
                color = Color(0xffff6e25),
                hoverInteraction = "outline",
                onClick = onPositive
            )
        }
    }
}
