import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.*
import arctic.states.Arctic
import arctic.states.ArcticState
import arctic.states.EditorState
import arctic.ui.composables.TitleView
import arctic.ui.composables.BottomBar
import arctic.ui.composables.inventory.InventoryView
import arctic.ui.composables.overlays.EditorOverlays
import arctic.ui.composables.overlays.GlobalOverlays
import arctic.ui.composables.overlays.SizeMeasureDummy
import arctic.ui.composables.overlays.extended.tween250
import arctic.ui.unit.dp

@Composable
@Preview
fun App() {
    val overlayState = Arctic.overlayState
    val pakIndexing = Arctic.pakState != ArcticState.PakState.Initialized

    val blur by animateDpAsState(
        targetValue =
            if (overlayState.visible && overlayState.nested || pakIndexing) 100.dp
            else if (overlayState.visible) 50.dp
            else 0.dp,
        tween(durationMillis = 250)
    )

    AppRoot(modifier = Modifier.onKeyEvent { Arctic.overlayState.pop(it.key) }) {
        TitleView(modifier = Modifier/*.blur(blur)*/.graphicsLayer { renderEffect = if (blur != 0.dp) BlurEffect(blur.value, blur.value) else null })
        MainContainer(modifier = Modifier/*.blur(blur)*/.graphicsLayer { renderEffect = if (blur != 0.dp) BlurEffect(blur.value, blur.value) else null }) {
            EditorAnimator(Arctic.editorState) { editor -> InventoryView(editor) }
            BottomBarAnimator(Arctic.editorState) { editor -> BottomBar(editor) }
        }
        EditorOverlayAnimator(Arctic.editorState) { editor -> EditorOverlays(editor) }
        GlobalOverlays()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.onGlobalPointerEvent(): Modifier {
    var parent = this
    for ((event, handlers) in Arctic.GlobalPointerListener.entries) {
        parent = parent.onPointerEvent(event) { handlers.forEach { handler -> handler(it) } }
    }
    return parent
}

@Composable
fun AppRoot(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) =
    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff272727))
            .onGlobalPointerEvent()
            .then(modifier),
        content = content
    )

@Composable
fun MainContainer(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().then(modifier),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomBarAnimator(targetState: EditorState?, content: @Composable BoxScope.(EditorState) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn(tween(250)) + slideIn(tween(250), initialOffset = { IntOffset(0, 20.dp.value.toInt()) })
            val exit = fadeOut(tween(250)) + slideOut(tween(250), targetOffset = { IntOffset(0, 20.dp.value.toInt()) })
            enter with exit using SizeTransform(false) { _, _ -> tween(durationMillis = 250) }
        },
        modifier = Modifier.fillMaxWidth(),
        content = {
            if (it != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().requiredHeight(85.dp),
                    content = { content(it) }
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth())
            }
        }
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S>ColumnScope.EditorAnimator(targetState: S?, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn(tween250()) + slideIn(tween250(), initialOffset = { IntOffset(0, -50.dp.value.toInt()) })
            val exit = fadeOut(tween250()) + slideOut(tween250(), targetOffset = { IntOffset(0, -50.dp.value.toInt()) })
            enter with exit using SizeTransform(false)
        },
        modifier = Modifier.weight(1f).fillMaxWidth(0.7638888f).offset(x = (-35).dp),
        content = { ContentContainer { if (it != null) content(it) else SizeMeasureDummy() } }
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun EditorOverlayAnimator(targetState: EditorState?, content: @Composable (EditorState) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = { fadeIn() with fadeOut() using SizeTransform(false) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (it != null) content(it)
        else SizeMeasureDummy()
    }

@Composable
private fun ContentContainer(content: @Composable RowScope.() -> Unit) =
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
        content = content
    )

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(1800.dp, 1400.dp), position = WindowPosition(Alignment.Center)),
        resizable = false,
        title = "Dungeons Editor",
        icon = painterResource("_icon.png")
    ) {
        App()
    }
}
