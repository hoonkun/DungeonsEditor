package kiwi.hoonkun.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.ui.composables.editor.EditorBottomBar
import kiwi.hoonkun.ui.composables.editor.collections.EquippedItems
import kiwi.hoonkun.ui.composables.editor.collections.InventoryItems
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.reusables.defaultSlideIn
import kiwi.hoonkun.ui.reusables.defaultSlideOut
import kiwi.hoonkun.ui.states.DungeonsJsonState
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.rememberEditorState
import kiwi.hoonkun.ui.units.dp

@Composable
fun JsonEditor(
    json: DungeonsJsonState?,
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
                val enter = defaultFadeIn() + defaultSlideIn { IntOffset(0, 20.dp.value.toInt()) }
                val exit = defaultFadeOut() + defaultSlideOut { IntOffset(0, -20.dp.value.toInt()) }
                enter togetherWith exit using SizeTransform(clip = false)
            },
            modifier = Modifier.fillMaxHeight()
        ) {
            if (it == null) placeholder()
            else JsonEditorContent(it)
        }
    }
}

@Composable
fun JsonEditorContent(json: DungeonsJsonState) {
    val editorState = rememberEditorState(json)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = editorState.view,
                transitionSpec = {
                    val a = if (targetState == EditorState.EditorView.Inventory) -50 else 50
                    val b = if (targetState == EditorState.EditorView.Inventory) 50 else -50
                    val enter = defaultFadeIn() + defaultSlideIn { IntOffset(a.dp.value.toInt(), 0) }
                    val exit = defaultFadeOut() + defaultSlideOut { IntOffset(b.dp.value.toInt(), 0) }
                    enter togetherWith exit using SizeTransform(false)
                }
            ) { view ->
                Column(
                    modifier = Modifier
                        .width(650.dp)
                        .padding(horizontal = 50.dp)
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
                            selection = editorState.selection,
                            noSpaceInInventory = editorState.noSpaceInInventory
                        )
                    } else {
                        InventoryItems(
                            items = editorState.stored.storageChestItems,
                            selection = editorState.selection,
                            noSpaceInInventory = editorState.noSpaceInInventory
                        )
                    }
                }
            }
        }
        EditorBottomBar(editorState)
    }
}