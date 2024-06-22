package kiwi.hoonkun.ui.composables

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.ui.composables.editor.EditorBottomBar
import kiwi.hoonkun.ui.composables.editor.collections.EquippedItems
import kiwi.hoonkun.ui.composables.editor.collections.InventoryItems
import kiwi.hoonkun.ui.composables.editor.details.ItemComparator
import kiwi.hoonkun.ui.composables.editor.details.Tips
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.states.DungeonsJsonState
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.rememberEditorState
import kiwi.hoonkun.ui.units.dp

@Composable
fun JsonEditor(
    json: DungeonsJsonState?,
    requestClose: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = { }
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .then(modifier)
            .background(Color(0xff202020))
    ) {
        AnimatedContent(
            targetState = json,
            transitionSpec = {
                val enter = defaultFadeIn() + slideIn { IntOffset(0, 20.dp.value.toInt()) }
                val exit = defaultFadeOut() + slideOut { IntOffset(0, -20.dp.value.toInt()) }
                enter togetherWith exit using SizeTransform(clip = false)
            },
            modifier = Modifier.fillMaxHeight()
        ) {
            if (it == null) placeholder()
            else Content(it, requestClose)
        }
    }
}

@Composable
private fun Content(
    json: DungeonsJsonState,
    requestClose: () -> Unit
) {
    val editorState = rememberEditorState(json)

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