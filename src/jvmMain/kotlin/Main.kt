import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.*
import androidx.compose.ui.zIndex
import kiwi.hoonkun.core.AppCompositionLocals
import kiwi.hoonkun.core.LocalWindowState
import kiwi.hoonkun.core.PakIndexingState
import kiwi.hoonkun.core.rememberPakIndexingState
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.resources.Resources
import kiwi.hoonkun.ui.composables.JsonEditor
import kiwi.hoonkun.ui.composables.JsonEntries
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.composables.overlays.ExitApplicationConfirmOverlay
import kiwi.hoonkun.ui.composables.overlays.SettingsOverlay
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.AppState
import kiwi.hoonkun.ui.states.LocalAppState
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures


fun main() = application {
    val windowSize = remember(0xC0FFEE.dp) { DpSize(1800.dp, 1400.dp) }
    val windowState = rememberWindowState(
        size = windowSize,
        position = WindowPosition(Alignment.Center)
    )

    SideEffect {
        windowState.size = windowSize
        windowState.position = WindowPosition(Alignment.Center)
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        resizable = false,
        title = "Dungeons Editor",
        icon = Resources.Drawables.icon(),
    ) {
        App(windowState = windowState, scope = this@application)
    }
}

@Composable
private fun App(
    windowState: WindowState,
    scope: ApplicationScope?
) {
    val overlays = LocalOverlayState.current

    val pakIndexingState by rememberPakIndexingState()

    val blur by minimizableAnimateFloatAsState(
        targetValue = if (overlays.any { it.backdropOptions.blur }) 50f else 0f,
        animationSpec = minimizableSpecDefault()
    )

    AppCompositionLocals(
        windowState = windowState,
        scope = scope
    ) {
        AppRoot {
             MinimizableAnimatedVisibility(
                visible = pakIndexingState == PakIndexingState.Loaded,
                enter = minimizableEnterTransition { expandIn() + fadeIn() },
                exit = minimizableExitTransition { shrinkOut() + fadeOut() },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { renderEffect = if (blur == 0f) null else BlurEffect(blur, blur) }
            ) {
                Background()
                AppContent()
            }
        }
    }
}

@Composable
private fun AppRoot(
    content: @Composable BoxScope.() -> Unit
) {
    val overlays = LocalOverlayState.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff272727))
            .onKeyEvent { if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) overlays.pop() else false }
    ) {
        content()
        overlays.Stack()
    }
}

@Composable
private fun AppContent() {
    val appState = LocalAppState.current

    val windowWidth = LocalWindowState.current.size.width

    val containerOffset by minimizableAnimateDpAsState(
        targetValue = (windowWidth * (AppState.Constants.SlidingRatio / 2)) * if (appState.isInEditor) -1 else 1,
        animationSpec = minimizableSpec { spring(stiffness = Spring.StiffnessLow) }
    )

    Row(
        modifier = Modifier
            .requiredWidth(windowWidth * (1 + AppState.Constants.SlidingRatio))
            .offset { IntOffset(containerOffset.roundToPx(), 0) }
    ) {
        val entriesOffset by minimizableAnimateDpAsState(
            targetValue = if (appState.isInEditor) (windowWidth * AppState.Constants.SlidingRatio) - 32.dp else 0.dp,
            animationSpec = minimizableSpec { spring(stiffness = Spring.StiffnessLow) }
        )
        val entriesBrightness by minimizableAnimateFloatAsState(
            targetValue = if (appState.isInEditor) 0.75f else 0f,
            animationSpec = minimizableSpec { spring() }
        )

        JsonEntries(
            onJsonSelect = { appState.sketchEditor(it) },
            preview = appState.editorCandidate,
            focused = !appState.isInEditor,
            modifier = Modifier
                .width(AppState.Constants.EntriesWidth)
                .offset { IntOffset(entriesOffset.roundToPx(), 0) }
                .drawWithContent {
                    drawContent()
                    drawRect(Color.Black, alpha = entriesBrightness)
                },
        )
        JsonEditor(
            state = appState.activeEditor,
            modifier = Modifier.weight(1f),
            onPreviewChange = { appState.editorCandidate = it.preview() }
        )
    }
}

@Composable
private fun Background() {
    Image(
        bitmap = DungeonsTextures["/UI/Materials/LoadingScreens/Loading_Ancient_Hunt.png"],
        contentScale = ContentScale.Crop,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                renderEffect = BlurEffect(50.dp.value, 50.dp.value)
            }
            .drawWithContent {
                drawContent()
                drawRect(Color.Black.copy(alpha = 0.6f))
            }
    )
}

@Composable
fun MainMenuButtons(
    description: String? = Localizations["exit_application_description"],
) {
    val appState = LocalAppState.current
    val overlays = LocalOverlayState.current

    MainIconButton(
        bitmap = Resources.Drawables.leave,
        onClick = {
            overlays.make { requestClose ->
                ExitApplicationConfirmOverlay(
                    description = description,
                    onConfirm = appState::exitApplication,
                    requestClose = requestClose
                )
            }
        }
    )
    Spacer(modifier = Modifier.width(16.dp))
    MainIconButton(
        bitmap = Resources.Drawables.settings,
        onClick = { overlays.make { SettingsOverlay() } }
    )
}

@Composable
private fun MainIconButton(
    bitmap: ImageBitmap,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RetroButton(
        color = Color(0xff434343),
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier
            .size(54.dp)
            .zIndex(2f)
            .then(modifier),
        onClick = { onClick() }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            filterQuality = FilterQuality.None
        )
    }
}
