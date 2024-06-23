package kiwi.hoonkun.ui.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonDpCornerRadius
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.composables.editor.EditorBottomBar
import kiwi.hoonkun.ui.composables.editor.collections.EquippedItems
import kiwi.hoonkun.ui.composables.editor.collections.InventoryItems
import kiwi.hoonkun.ui.composables.editor.details.ItemComparator
import kiwi.hoonkun.ui.composables.editor.details.Tips
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.units.dp

@Composable
fun JsonEditor(
    state: EditorState?,
    requestClose: () -> Unit,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit = { },
    placeholder: @Composable () -> Unit = { }
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .then(modifier)
            .background(Color(0xff202020))
    ) {
        tabs()
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                val floatSpec = spring<Float>(stiffness = Spring.StiffnessLow)
                val intOffsetSpec = spring<IntOffset>(stiffness = Spring.StiffnessLow)
                val enter = fadeIn(floatSpec) + slideIn(intOffsetSpec) { IntOffset(0, 80.dp.value.toInt()) }
                val exit = fadeOut() + slideOut(intOffsetSpec) { IntOffset(0, 160.dp.value.toInt()) }
                enter togetherWith exit using SizeTransform(clip = false)
            },
            modifier = Modifier.fillMaxHeight().background(Color(0xff202020))
        ) {
            if (it == null) placeholder()
            else Content(it, requestClose)
        }
    }
}

@Composable
fun JsonEditorTabButton(
    bitmap: ImageBitmap,
    selected: Boolean,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    onClick: () -> Unit
) {
    val color by animateColorAsState(if (selected) Color(0xff3f8e4f) else Color(0xff363636))

    RetroButton(
        color = { color },
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        contentPadding = contentPadding,
        radius = RetroButtonDpCornerRadius(8.dp, 0.dp, 8.dp, 0.dp),
        modifier = Modifier.fillMaxWidth().offset(x = 4.dp),
        onClick = onClick
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun Content(
    editorState: EditorState,
    requestClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 100.dp, end = 170.dp)
        ) {
            AnimatedContent(
                targetState = editorState.view,
                transitionSpec = {
                    val a = if (targetState == EditorState.EditorView.Inventory) -50 else 50
                    val b = if (targetState == EditorState.EditorView.Inventory) 50 else -50
                    val enter = defaultFadeIn() + slideIn { IntOffset(a.dp.value.toInt(), 0) }
                    val exit = defaultFadeOut() + slideOut { IntOffset(b.dp.value.toInt(), 0) }
                    enter togetherWith exit using SizeTransform(false)
                }
            ) { view ->
                Column(
                    modifier = Modifier
                        .width(650.dp)
                        .padding(top = 25.dp)
                ) {
                    if (view == EditorState.EditorView.Inventory) {
                        EquippedItems(
                            items = editorState.stored.equippedItems,
                            selection = editorState.selection
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(start = 80.dp, end = 10.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        )
                        InventoryItems(
                            items = editorState.stored.unequippedItems,
                            editorState = editorState,
                        )
                    } else {
                        InventoryItems(
                            items = editorState.stored.storageChestItems,
                            editorState = editorState
                        )
                    }
                }
            }
            AnimatedContent(
                targetState = editorState.selection.hasSelection,
                transitionSpec = {
                    val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
                    val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
                    enter togetherWith exit using SizeTransform(clip = false)
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(start = 50.dp)
                ) {
                    if (it) {
                        ItemComparator(editor = editorState)
                    } else {
                        Tips()
                    }
                }
            }
        }
        EditorBottomBar(editorState, requestClose)
    }
}