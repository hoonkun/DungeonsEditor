package arctic.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import arctic.states.Arctic
import arctic.states.EditorState
import arctic.ui.unit.dp
import arctic.ui.composables.atomic.RetroButton
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
            title = "${where.localizedName}에 있는 아이템이에요. 어디에 복제하시겠어요?",
            description = "지금은 ${editor.view.localizedName}를 보고있어요."
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = "원래 위치에 복제",
                color = Color(0xff3f8e4f),
                hoverInteraction = "outline",
                onClick = onOriginalSelected
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = "취소",
                color = Color(0xffffffff),
                hoverInteraction = "overlay",
                onClick = onClose
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = "여기에 복제",
                color = Color(0xff3f8e4f),
                hoverInteraction = "outline",
                onClick = onHereSelected
            )
        }
    }
}
