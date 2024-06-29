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
import kiwi.hoonkun.ui.states.DungeonsJsonEditorState
import kiwi.hoonkun.ui.states.DungeonsJsonEditorState.EditorView.Companion.toEditorView
import kiwi.hoonkun.ui.states.OverlayCloser
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.withItemManager

@Composable
fun ItemDuplicateLocationConfirmOverlay(
    editor: DungeonsJsonEditorState,
    target: MutableDungeons.Item,
    requestClose: OverlayCloser
) {
    val onClose = { requestClose() }

    val onOriginalSelected = {
        editor.selection.replace(
            from = target,
            new = withItemManager { editor.stored.duplicate(target) }
        )
        onClose()
    }

    val onHereSelected = {
        val created = withItemManager { editor.stored.add(target.copy(), editor.view.toItemLocation()) }

        editor.selection.clear()
        editor.selection.select(
            item = created,
            into = DungeonsJsonEditorState.SelectionState.Slot.Primary
        )
        onClose()
    }

    val where = withItemManager { editor.stored.locationOf(target).toEditorView() }

    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations["inventory_duplicate_title", where.localizedName],
            description = Localizations["inventory_duplicate_description", editor.view.localizedName]
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = Localizations["inventory_duplicate_button_source"],
                color = Color(0xff3f8e4f),
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = onOriginalSelected
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = Localizations["cancel"],
                color = Color(0xffffffff),
                hoverInteraction = RetroButtonHoverInteraction.Overlay,
                onClick = onClose
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = Localizations["inventory_duplicate_button_here"],
                color = Color(0xff3f8e4f),
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = onHereSelected
            )
        }
    }
}
