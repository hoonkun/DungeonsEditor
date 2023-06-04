package arctic.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import arctic.states.arctic
import arctic.ui.unit.dp
import arctic.ui.composables.atomic.RetroButton
import dungeons.states.Item
import dungeons.states.extensions.addItem
import dungeons.states.extensions.where

@Composable
fun ItemDuplicateLocationConfirmOverlay() {
    val target = arctic.duplication.target

    OverlayBackdrop(target != null) { arctic.duplication.target = null }
    OverlayAnimator(target to target?.where) { (target, where) ->
        if (target != null && where != null) Content(target, where)
        else SizeMeasureDummy()
    }
}

@Composable
private fun Content(target: Item, where: String) {

    val onClose = { arctic.duplication.target = null }

    val onOriginalSelected = {
        target.parent.addItem(target.copy(), target)
        onClose()
    }

    val onHereSelected = {
        target.parent.addItem(target.copy())
        onClose()
    }

    ContentRoot {
        OverlayTitleDescription(
            title = "${whereName(where)}에 있는 아이템이에요. 어디에 복제하시겠어요?",
            description = "지금은 ${whereName(arctic.view)}를 보고있어요."
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
