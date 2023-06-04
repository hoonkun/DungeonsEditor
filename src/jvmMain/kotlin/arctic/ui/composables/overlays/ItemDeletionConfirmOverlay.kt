package arctic.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import arctic.states.arctic
import arctic.ui.unit.dp
import arctic.ui.composables.atomic.RetroButton
import dungeons.states.Item
import dungeons.states.extensions.deleteItem
import dungeons.states.extensions.where

@Composable
fun ItemDeletionConfirmOverlay() {
    val target = arctic.deletion.target

    OverlayBackdrop(target != null) { arctic.deletion.target = null }
    OverlayAnimator(target to target?.where) { (target, where) ->
        if (target != null && where != null) Content(target, where)
        else SizeMeasureDummy()
    }
}

@Composable
private fun Content(target: Item, where: String) {

    val onNegative = { arctic.deletion.target = null }
    val onPositive = {
        target.parent.deleteItem(target)
        arctic.deletion.target = null
    }

    ContentRoot {
        OverlayTitleDescription(
            title = "${if (where != arctic.view) "${whereName(where)}에 있는 아이템이에요." else ""} 정말 이 아이템을 삭제하시겠어요?",
            description = "게임 내에서 분해하면 에메랄드 보상을 받을 수 있지만, 여기서는 받을 수 없어요."
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = "취소",
                color = Color(0xffffffff),
                hoverInteraction = "overlay",
                onClick = onNegative
            )
            Spacer(modifier = Modifier.width(150.dp))
            RetroButton(
                text = "삭제",
                color = Color(0xffff6e25),
                hoverInteraction = "outline",
                onClick = onPositive
            )
        }
    }
}
