package composable.inventory

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.IntOffset
import blackstone.states.dp
import blackstone.states.sp
import arctic
import blackstone.states.items.unequipped
import blackstone.states.Item
import blackstone.states.items.equippedItems
import composable.RetroButton

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RowScope.InventoryView() {
    Debugging.recomposition("InventoryView")

    AnimatedContent(arctic.stored) { stored ->
        if (stored != null) {
            AnimatedContent(
                targetState = stored to arctic.view,
                transitionSpec = {
                    val enter = fadeIn(tween(durationMillis = 250)) + slideIn(
                        tween(durationMillis = 250),
                        initialOffset = { IntOffset(50, 0) })
                    val exit = fadeOut(tween(durationMillis = 250)) + slideOut(
                        tween(durationMillis = 250),
                        targetOffset = { IntOffset(-50, 0) })
                    enter with exit using SizeTransform(false) { _, _ -> tween(durationMillis = 250) }
                },
                modifier = Modifier.width(657.dp)
            ) { (stored, view) ->
                Row(modifier = Modifier.fillMaxSize()) {
                    if (view == "inventory") {
                        LeftArea {
                            EquippedItems(stored.equippedItems)
                            Divider()
                            InventoryItems(stored.items.filter(unequipped))
                        }
                    } else if (view == "storage") {
                        LeftArea { InventoryItems(stored.storageChestItems) }
                    }
                }
            }
        }
    }
    RightArea {
        AnimatorBySelectedItemExists(arctic.selection.selected) {
            if (it.any { item -> item != null }) ItemComparatorView(it)
            else NoItemsSelectedView()
        }
    }
}

@Composable
private fun LeftArea(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxHeight().padding(top = 20.dp),
        content = content
    )

@Composable
private fun RowScope.RightArea(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxHeight().width(718.dp).padding(top = 20.dp, bottom = 20.dp, start = 75.dp),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S> AnimatorBySelectedItemExists(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter with exit
        },
        content = content
    )

@Composable
private fun NoItemsSelectedView() =
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        RetroButton("파일 선택", color = Color(0xff3f8e4f), hoverInteraction = "outline") {
            arctic.dialogs.fileLoadSrcSelector = true
        }
    }

@Composable
private fun ItemComparatorView(items: List<Item?>) {
    Debugging.recomposition("ItemComparatorView")

    val scroll = rememberScrollState()
    val adapter = rememberScrollbarAdapter(scroll)
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll), verticalArrangement = Arrangement.Center) {
            AnimatedItemDetailView(items[0])
            Spacer(modifier = Modifier.height(20.dp))
            AnimatedItemDetailView(items[1])
        }
        VerticalScrollbar(
            adapter = adapter,
            style = GlobalScrollbarStyle,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 20.dp)
        )
    }
}

val GlobalScrollbarStyle = ScrollbarStyle(
    thickness = 20.dp,
    minimalHeight = 100.dp,
    hoverColor = Color.White.copy(alpha = 0.3f),
    unhoverColor = Color.White.copy(alpha = 0.15f),
    hoverDurationMillis = 0,
    shape = RoundedCornerShape(3.dp),
)
