package kiwi.hoonkun.ui.composables.editor.collections

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
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
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures

@Composable
fun EquippedItems(items: List<Item?>, selection: EditorState.SelectionState) {
    var collapsed by remember { mutableStateOf(false) }

    Row {
        EquippedItemCollectionToggleButton(collapsed) { collapsed = !collapsed }
        EquippedItemCollectionToggleAnimator(collapsed) { collapsed ->
            ItemsLazyGrid(
                columns = if (collapsed) 6 else 3,
                items = items
            ) { _, item ->
                ItemGridItem(item, collapsed, selection)
            }
        }
    }
}

@Composable
private fun EquippedItemCollectionToggleAnimator(
    targetState: Boolean,
    content: @Composable AnimatedVisibilityScope.(Boolean) -> Unit
) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { -it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter togetherWith exit using SizeTransform(false)
        },
        content = content
    )

@Composable
private fun EquippedItemCollectionToggleButton(collapsed: Boolean, onClick: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val bitmap = remember { DungeonsTextures["/Game/UI/Materials/Menu/arrow_gamemode.png"] }

    val rotation by animateFloatAsState(if (collapsed) 180f else 0f)

    Image(
        bitmap = bitmap,
        contentDescription = null,
        alpha = if (collapsed) 1.0f else if (hovered) 0.5f else 0.3f,
        modifier = Modifier
            .size(70.dp)
            .offset(y = 15.dp)
            .clickable(interaction, null, onClick = onClick)
            .hoverable(interaction)
            .padding(top = 15.dp, end = 15.dp, bottom = 15.dp, start = 25.dp)
            .graphicsLayer { rotationZ = rotation }
    )
}