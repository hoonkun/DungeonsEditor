package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.withItemManager


@Composable
fun ItemDeleteConfirmOverlay(
    editor: EditorState,
    target: MutableDungeons.Item,
    requestClose: () -> Unit
) {
    val onNegative = { requestClose() }
    val onPositive = {
        editor.deselect(target)
        withItemManager { editor.data.remove(target) }
        requestClose()
    }

    val locationOfTarget = remember(target) { withItemManager { editor.data.locationOf(target) } }

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
