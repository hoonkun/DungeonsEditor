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
fun ItemDeleteConfirmOverlay(
    editor: EditorState,
    target: Item,
    requestClose: () -> Unit
) {
    val onNegative = { requestClose() }
    val onPositive = {
        target.parent.deleteItem(editor, target)
        requestClose()
    }

    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText(
                key = "inventory_delete_title",
                if (target.where != editor.view)
                    Localizations.UiText(
                        key = "inventory_delete_title_arg",
                        target.where?.localizedName ?: Localizations.UiText("unknown_place")
                    )
                else
                    ""
            ),
            description = Localizations.UiText("inventory_delete_description")
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = Localizations.UiText("cancel"),
                color = Color(0xffffffff),
                hoverInteraction = RetroButtonHoverInteraction.Overlay,
                onClick = onNegative,
                modifier = Modifier.size(200.dp, 65.dp)
            )
            Spacer(modifier = Modifier.width(150.dp))
            RetroButton(
                text = Localizations.UiText("delete"),
                color = Color(0xffff6e25),
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = onPositive,
                modifier = Modifier.size(200.dp, 65.dp)
            )
        }
    }
}
