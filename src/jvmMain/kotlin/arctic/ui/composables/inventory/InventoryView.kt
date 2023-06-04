package arctic.ui.composables.inventory

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import arctic.states.arctic
import arctic.ui.composables.inventory.collections.EquippedItemCollection
import arctic.ui.composables.inventory.collections.UnequippedItemCollection
import arctic.ui.composables.overlays.SizeMeasureDummy
import arctic.ui.composables.overlays.extended.tween250
import arctic.ui.unit.dp
import composable.inventory.InventoryItems
import dungeons.states.DungeonsJsonState
import dungeons.states.extensions.equippedItems
import dungeons.states.extensions.unequippedItems

@Composable
fun InventoryView() {
    RootAnimator(arctic.stored) { stored ->
        if (stored != null) Content(stored)
        else SizeMeasureDummy()
    }
}

@Composable
private fun Content(stored: DungeonsJsonState) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        LeftAreaAnimator(stored to arctic.view) { (stored, view) ->
            LeftArea {
                if (view == "inventory") {
                    EquippedItemCollection(stored.equippedItems)
                    Divider()
                    UnequippedItemCollection(stored.unequippedItems)
                } else if (view == "storage") {
                    InventoryItems(stored.storageChestItems)
                }
            }
        }
        RightAreaAnimator(arctic.selection.selected.any { item -> item != null }) {
            RightArea {

            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S>RootAnimator(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn(tween250()) + slideIn(tween250(), initialOffset = { IntOffset(0, -50.dp.value.toInt()) })
            val exit = fadeOut(tween250()) + slideOut(tween250(), targetOffset = { IntOffset(0, -50.dp.value.toInt()) })
            enter with exit using SizeTransform(false)
        },
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S>LeftAreaAnimator(targetState: S, content: AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn(tween250()) + slideIn(tween250(), initialOffset = { IntOffset(50.dp.value.toInt(), 0) })
            val exit = fadeOut(tween250()) + slideOut(tween250(), targetOffset = { IntOffset(-50.dp.value.toInt(), 0) })
            enter with exit using SizeTransform(false)
        },
        modifier = Modifier.width(657.dp),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S>RightAreaAnimator(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter with exit
        },
        modifier = Modifier.fillMaxHeight().width(718.dp),
        content = content
    )


@Composable
private fun LeftArea(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 20.dp),
        content = content
    )

@Composable
private fun RightArea(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxSize(),
        content = content
    )

@Composable
fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(start = 60.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
            .background(Color(0xff666666))
    )
}
