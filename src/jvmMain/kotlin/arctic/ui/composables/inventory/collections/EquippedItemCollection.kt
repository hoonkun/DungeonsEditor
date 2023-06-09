package arctic.ui.composables.inventory.collections

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
import androidx.compose.ui.draw.rotate
import arctic.states.EditorSelectionState
import arctic.ui.unit.dp
import arctic.ui.utils.rememberMutableInteractionSource
import dungeons.IngameImages
import dungeons.states.Item

@Composable
fun EquippedItemCollection(items: List<Item?>, selection: EditorSelectionState) {
    var collapsed by remember { mutableStateOf(false) }

    Row {
        EquippedItemCollectionToggleButton(collapsed) { collapsed = !collapsed }
        EquippedItemCollectionToggleAnimator(collapsed) { collapsed ->
            ItemsLazyGrid(columns = if (collapsed) 6 else 3, items = items) { _, item ->
                ItemGridItem(item, collapsed, selection)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
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
            enter with exit using SizeTransform(false)
        },
        content = content
    )

@Composable
private fun EquippedItemCollectionToggleButton(collapsed: Boolean, onClick: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val bitmap = remember { IngameImages.get { "/Game/UI/Materials/Menu/arrow_gamemode.png" } }

    val rotation by animateFloatAsState(if (collapsed) 180f else 0f)

    Image(
        bitmap = bitmap,
        contentDescription = null,
        alpha = if (collapsed) 1.0f else if (hovered) 0.5f else 0.3f,
        modifier = Modifier
            .size(60.dp)
            .offset(y = 10.dp)
            .clickable(interaction, null, onClick = onClick)
            .hoverable(interaction)
            .padding(15.dp)
            .rotate(rotation)
    )
}
