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
fun ItemDeleteConfirmOverlay(
    editor: DungeonsJsonEditorState,
    target: MutableDungeons.Item,
    requestClose: OverlayCloser
) {
    val onNegative = { requestClose() }
    val onPositive = {
        editor.selection.unselect(target)
        withItemManager { editor.stored.remove(target) }
        requestClose()
    }

    val locationOfTarget = withItemManager { editor.stored.locationOf(target).toEditorView() }

    val differenceLocationDescription =
        if (locationOfTarget != editor.view)
            Localizations["inventory_delete_title_arg", locationOfTarget.localizedName]
        else
            ""

    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations["inventory_delete_title", differenceLocationDescription],
            description = Localizations["inventory_delete_description"]
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = Localizations["cancel"],
                color = Color(0xffffffff),
                hoverInteraction = RetroButtonHoverInteraction.Overlay,
                onClick = onNegative,
            )
            Spacer(modifier = Modifier.width(150.dp))
            RetroButton(
                text = Localizations["delete"],
                color = Color(0xffff6e25),
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = onPositive,
            )
        }
    }
}
