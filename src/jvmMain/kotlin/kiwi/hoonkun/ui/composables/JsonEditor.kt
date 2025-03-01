package kiwi.hoonkun.ui.composables

import MainMenuButtons
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kiwi.hoonkun.core.LocalWindowState
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.resources.Resources
import kiwi.hoonkun.ui.composables.base.FileSelector
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonDpCornerRadius
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.composables.editor.EditorBottomBar
import kiwi.hoonkun.ui.composables.editor.collections.EquippedItems
import kiwi.hoonkun.ui.composables.editor.collections.InventoryItems
import kiwi.hoonkun.ui.composables.editor.details.ItemComparator
import kiwi.hoonkun.ui.composables.editor.details.Tips
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.AppState
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.LocalAppState
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.io.DungeonsJsonFile
import minecraft.dungeons.resources.DungeonsTextures

@Composable
fun JsonEditor(
    state: EditorState?,
    modifier: Modifier = Modifier,
    onPreviewChange: (DungeonsJsonFile) -> Unit
) {
    val appState = LocalAppState.current

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .then(modifier)
            .background(Color(0xff202020))
    ) {
        JsonEditorTabs()
        MinimizableAnimatedContent(
            targetState = state,
            transitionSpec = minimizableContentTransform spec@ {
                if (targetState == null || initialState == null)
                    return@spec fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) { _, _ -> snap() }

                val floatSpec = spring<Float>(stiffness = Spring.StiffnessLow)
                val intOffsetSpec = spring<IntOffset>(stiffness = Spring.StiffnessLow)
                val enter = fadeIn(floatSpec) + slideIn(intOffsetSpec) { IntOffset(0, 80.dp.value.toInt()) }
                val exit = fadeOut() + slideOut(intOffsetSpec) { IntOffset(0, 160.dp.value.toInt()) }

                enter togetherWith exit using SizeTransform(clip = false)
            },
            modifier = Modifier.fillMaxHeight().background(Color(0xff202020))
        ) { editor ->
            if (editor == null)
                JsonEditorFileSelector(
                    onSelectorTransform = onPreviewChange,
                    onSelectFile = { appState.sketchEditor(it.absolutePath) }
                )
            else
                JsonEditorContent(
                    editorState = editor,
                    requestClose = appState::eraseEditor
                )
        }
    }
}

@Composable
private fun JsonEditorFileSelector(
    onSelectorTransform: (DungeonsJsonFile) -> Unit,
    onSelectFile: (DungeonsJsonFile) -> Unit
) {
    val windowWidth = LocalWindowState.current.size.width

    Box(
        modifier = Modifier
            .requiredWidth(windowWidth - AppState.Constants.EntriesWidth)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawBehind {
                val image = DungeonsTextures["/UI/Materials/LoadingScreens/loadingscreen_subdungeon.png"]
                val dstSize = Size(size.width, size.width * (image.height.toFloat() / image.width)).round()

                drawRect(
                    Brush.verticalGradient(
                        0f to Color(0xff202020).copy(alpha = 0f),
                        1f to Color(0xff202020),
                        endY = dstSize.height.toFloat(),
                        tileMode = TileMode.Clamp
                    )
                )
                drawImage(
                    image = image,
                    dstSize = dstSize,
                    blendMode = BlendMode.SrcOut
                )
            }
            .padding(horizontal = 40.dp)
            .padding(bottom = 36.dp)
            .fillMaxHeight()
    ) {
        Row(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            MainMenuButtons()
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .zIndex(1f)
                .alpha(0.5f)
        ) {
            Text(text = "Dungeons Editor, 1.1.0 by HoonKun", fontFamily = Resources.Fonts.JetbrainsMono)
            Text(
                text = "Compatible with Minecraft Dungeons 1.17.0.0",
                fontFamily = Resources.Fonts.JetbrainsMono
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            FileSelector(
                validator = { it.validate() },
                transform = { DungeonsJsonFile(it).also(onSelectorTransform) },
                onSelect = { onSelectFile(it) },
                buttonText = Localizations["open"],
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(525.dp)
                    .padding(top = 32.dp)
                    .offset(y = 110.dp),
            )
        }
    }
}

@Composable
private fun JsonEditorTabs() {
    val appState = LocalAppState.current

    val tabsOffset by minimizableAnimateDpAsState(
        targetValue = if (appState.isInEditor || appState.openedEditors.size > 0) (-114).dp else 0.dp,
        animationSpec = minimizableSpec { spring(stiffness = Spring.StiffnessLow) }
    )

    Column(
        modifier = Modifier
            .requiredWidth(114.dp)
            .padding(top = 24.dp, start = 10.dp)
            .offset { IntOffset(x = tabsOffset.roundToPx(), y = 0) }
    ) {
        JsonEditorTabButton(
            bitmap = DungeonsTextures["/UI/Materials/Map/Pins/mapicon_chest.png"],
            selected = appState.activeEditorKey == null,
            onClick = { appState.activeEditorKey = null },
            contentPadding = PaddingValues(16.dp)
        )
        appState.openedEditors.keys.forEach { key ->
            JsonEditorTabButton(
                bitmap = DungeonsTextures.Pets[key],
                selected = appState.activeEditorKey == key,
                onClick = { appState.activeEditorKey = key }
            )
        }
    }
}

@Composable
private fun JsonEditorTabButton(
    bitmap: ImageBitmap,
    selected: Boolean,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    onClick: () -> Unit
) {
    val color by minimizableAnimateColorAsState(
        targetValue = if (selected) Color(0xff3f8e4f) else Color(0xff363636),
        animationSpec = minimizableSpec { spring() }
    )

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
private fun JsonEditorContent(
    editorState: EditorState,
    requestClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 100.dp, end = 170.dp)
        ) {
            MinimizableAnimatedContent(
                targetState = editorState.view,
                transitionSpec = minimizableContentTransform spec@ {
                    val a = if (targetState.isInventory()) -50 else 50
                    val b = if (targetState.isInventory()) 50 else -50
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
                    if (view.isInventory()) {
                        EquippedItems(
                            items = editorState.data.equippedItems,
                            editor = editorState
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(start = 80.dp, end = 10.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        )
                        InventoryItems(
                            items = editorState.data.inventoryItems,
                            editorState = editorState,
                        )
                    } else {
                        InventoryItems(
                            items = editorState.data.storageItems,
                            editorState = editorState
                        )
                    }
                }
            }
            MinimizableAnimatedContent(
                targetState = editorState.hasSelection,
                transitionSpec = minimizableContentTransform spec@ {
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