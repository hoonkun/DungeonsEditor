package arctic.ui.composables.inventory

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import arctic.states.EditorState
import arctic.ui.composables.inventory.collections.EquippedItemCollection
import arctic.ui.composables.inventory.collections.UnequippedItemCollection
import arctic.ui.composables.inventory.details.ItemDetail
import arctic.ui.composables.overlays.extended.defaultFadeIn
import arctic.ui.composables.overlays.extended.defaultFadeOut
import arctic.ui.composables.overlays.extended.defaultSlideIn
import arctic.ui.composables.overlays.extended.defaultSlideOut
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.Localizations

@Composable
fun InventoryView(editor: EditorState) {
    Content(editor)
}

@Composable
private fun Content(editor: EditorState) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        LeftAreaAnimator(editor.view) { view ->
            LeftArea {
                if (view == EditorState.EditorView.Inventory) {
                    EquippedItemCollection(editor.stored.equippedItems, editor.selection)
                    Divider()
                    UnequippedItemCollection(editor.stored.unequippedItems, editor.selection, editor.noSpaceInInventory)
                } else if (view == EditorState.EditorView.Storage) {
                    UnequippedItemCollection(editor.stored.storageChestItems, editor.selection, editor.noSpaceInInventory)
                }
            }
        }
        RightAreaAnimator(editor.selection.hasSelection) {
            RightArea {
                if (it) ItemComparator(editor)
                else SomeTips()
            }
        }
    }
}

@Composable
private fun <S>LeftAreaAnimator(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = defaultFadeIn() + defaultSlideIn { IntOffset(50.dp.value.toInt(), 0) }
            val exit = defaultFadeOut() + defaultSlideOut { IntOffset(-50.dp.value.toInt(), 0) }
            enter togetherWith exit using SizeTransform(false)
        },
        modifier = Modifier.width(657.dp),
        content = content
    )

@Composable
private fun <S>RightAreaAnimator(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter togetherWith exit
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
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(start = 75.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
            .background(Color(0xff666666))
    )
}

@Composable
private fun SomeTips() =
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().offset(x = 35.dp).alpha(0.85f)
    ) {
        TipsTitle()
        for (i in 0 until 6) { Tip(Localizations.UiText("tips_${i}")) }
    }

@Composable
private fun TipsTitle() {
    Text(text = Localizations.UiText("tips_title"), fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 60.dp, vertical = 30.dp))
}

@Composable
private fun Tip(text: String) {
    Text(text = text, fontSize = 24.sp, color = Color.White, modifier = Modifier.padding(horizontal = 60.dp, vertical = 20.dp))
}

@Composable
private fun ItemComparator(editor: EditorState) {
    val scroll = rememberScrollState()
    val adapter = rememberScrollbarAdapter(scroll)
    Box(modifier = Modifier.fillMaxSize().padding(start = 75.dp)) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().verticalScroll(scroll)
        ) {
            ItemDetail(editor.selection.primary, editor)
            Spacer(modifier = Modifier.height(20.dp))
            ItemDetail(editor.selection.secondary, editor)
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
