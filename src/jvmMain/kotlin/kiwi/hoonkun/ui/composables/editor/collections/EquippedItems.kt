package kiwi.hoonkun.ui.composables.editor.collections

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures

@Composable
fun EquippedItems(items: List<Item?>, selection: EditorState.SelectionState) {
    var collapsed by remember { mutableStateOf(false) }

    Row {
        EquippedItemsToggleButton(collapsed) { collapsed = !collapsed }
        MinimizableAnimatedContent(
            targetState = collapsed,
            transitionSpec = minimizableContentTransform spec@ {
                val enter = slideInVertically(initialOffsetY = { -it / 10 }) + fadeIn()
                val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
                enter togetherWith exit using SizeTransform(false)
            }
        ) { collapsed ->
            ItemsLazyGrid(
                columns = if (collapsed) 6 else 3,
                items = items,
                itemContent = { item -> ItemGridItem(item, collapsed, selection) }
            )
        }
    }
}

@Composable
private fun EquippedItemsToggleButton(collapsed: Boolean, onToggle: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val rotation by minimizableAnimateFloatAsState(
        targetValue = if (collapsed) 180f else 0f,
        animationSpec = minimizableSpec { spring() }
    )

    Image(
        bitmap = DungeonsTextures["/Game/UI/Materials/Menu/arrow_gamemode.png"],
        contentDescription = null,
        modifier = Modifier
            .size(70.dp)
            .offset(y = 15.dp)
            .clickable(interaction, null, onClick = onToggle)
            .hoverable(interaction)
            .padding(top = 15.dp, end = 15.dp, bottom = 15.dp, start = 25.dp)
            .graphicsLayer {
                rotationZ = rotation
                alpha = if (collapsed) 1.0f else if (hovered) 0.5f else 0.3f
            }
    )
}
