package composable.inventory

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import editorState
import states.*
import stored

@Composable
fun RowScope.InventoryView() {
    val equipped by remember { derivedStateOf { stored.items.equipped } }
    val inventory by remember { derivedStateOf { stored.items.unequipped } }
    val selected by remember { derivedStateOf { editorState.inventoryState.selectedItems } }

    LeftArea {
        EquippedItems(equipped)
        Divider()
        InventoryItems(inventory)
    }
    RightArea {
        AnimatorBySelectedItemExists(selected.all { it == null }) {
            if (it) NoItemsSelectedView()
            else ItemComparatorView(selected)
        }
    }
}

@Composable
private fun RowScope.LeftArea(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxHeight().weight(0.45f).padding(top = 20.dp),
        content = content
    )

@Composable
private fun RowScope.RightArea(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxHeight().weight(0.55f).padding(top = 20.dp, bottom = 20.dp, start = 75.dp),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatorBySelectedItemExists(targetState: Boolean, content: @Composable AnimatedVisibilityScope.(Boolean) -> Unit) =
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
        Text("확인할 아이템을 선택해보세요!", color = Color.White, fontSize = 20.sp)
    }

@Composable
private fun ItemComparatorView(items: List<Item?>) {
    val scroll = rememberScrollState()
    val adapter = rememberScrollbarAdapter(scroll)
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll), verticalArrangement = Arrangement.Center) {
            AnimatedItemDetailView(items[0], "primary")
            AnimatedItemDetailView(items[1], "secondary")
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
