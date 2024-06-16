import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import arctic.states.Arctic
import arctic.states.EditorState
import arctic.ui.composables.BottomBar
import arctic.ui.composables.TitleView
import arctic.ui.composables.inventory.InventoryView
import arctic.ui.composables.overlays.EditorOverlays
import arctic.ui.composables.overlays.GlobalOverlays
import arctic.ui.composables.overlays.SizeMeasureDummy
import arctic.ui.composables.overlays.extended.*
import arctic.ui.unit.dp


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

@Composable
@Preview
private fun App() {
    val blur by animateDpAsState(
        targetValue =
            if (Arctic.overlayState.visible && Arctic.overlayState.nested || Arctic.pakState.isIndexing) 100.dp
            else if (Arctic.overlayState.visible) 50.dp
            else 0.dp,
        animationSpec = defaultTween()
    )

    AppRoot {
        TitleView(modifier = Modifier.blurEffect { blur.value })
        MainContainer(modifier = Modifier.blurEffect { blur.value }) {
            EditorAnimator(Arctic.editorState) { editor -> InventoryView(editor) }
            BottomBarAnimator(Arctic.editorState) { editor -> BottomBar(editor) }
        }
        EditorOverlayAnimator(Arctic.editorState) { editor -> EditorOverlays(editor) }
        GlobalOverlays()
    }
}

@Composable
private fun AppRoot(content: @Composable BoxScope.() -> Unit) =
    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff272727))
            .onGlobalPointerEvent()
            .onKeyEvent { Arctic.overlayState.pop(it.key) },
        content = content
    )

@Composable
private fun MainContainer(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().then(modifier),
        content = content
    )

@Composable
private fun BottomBarAnimator(targetState: EditorState?, content: @Composable BoxScope.(EditorState) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = defaultFadeIn() + defaultSlideIn { IntOffset(0, 20.dp.value.toInt()) }
            val exit = defaultFadeOut() + defaultSlideOut { IntOffset(0, 20.dp.value.toInt()) }
            enter togetherWith exit using SizeTransform(false) { _, _ -> defaultTween() }
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

@Composable
private fun <S>ColumnScope.EditorAnimator(targetState: S?, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = defaultFadeIn() + defaultSlideIn { IntOffset(0, -50.dp.value.toInt()) }
            val exit = defaultFadeOut() + defaultSlideOut { IntOffset(0, -50.dp.value.toInt()) }
            enter togetherWith exit using SizeTransform(false)
        },
        modifier = Modifier.weight(1f).fillMaxWidth(0.7638888f).offset(x = (-35).dp),
        content = { ContentContainer { if (it != null) content(it) else SizeMeasureDummy() } }
    )

@Composable
private fun EditorOverlayAnimator(targetState: EditorState?, content: @Composable (EditorState) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(false) },
        modifier = Modifier.fillMaxSize(),
        content = { if (it != null) content(it) else SizeMeasureDummy() }
    )

@Composable
private fun ContentContainer(content: @Composable RowScope.() -> Unit) =
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
        content = content
    )

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.onGlobalPointerEvent(): Modifier =
    Arctic.GlobalPointerListener.entries.fold(this) { acc, (event, handlers) ->
        acc.onPointerEvent(event) { scope -> handlers.forEach { it(scope) } }
    }

private fun Modifier.blurEffect(blurRadius: () -> Float) =
    graphicsLayer {
        val radius = blurRadius()
        if (radius == 0f) return@graphicsLayer

        renderEffect = BlurEffect(radius, radius)
    }
