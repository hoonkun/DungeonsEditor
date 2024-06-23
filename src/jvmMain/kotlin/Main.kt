import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kiwi.hoonkun.ArcticSave
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.JsonEditor
import kiwi.hoonkun.ui.composables.JsonEntries
import kiwi.hoonkun.ui.composables.base.FileSelector
import kiwi.hoonkun.ui.composables.overlays.PakIndexingOverlay
import kiwi.hoonkun.ui.composables.overlays.PakNotFoundOverlay
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.states.*
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.io.DungeonsJsonFile
import minecraft.dungeons.resources.DungeonsTextures


fun main() = application {
    val windowState = LocalWindowState.current

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        resizable = false,
        title = "Dungeons Editor",
        icon = painterResource("_icon.png"),
    ) {
        App(windowState.size.width)
    }
}

val LocalWindowState = staticCompositionLocalOf {
    WindowState(
        size = DpSize(1800.dp, 1400.dp),
        position = WindowPosition(Alignment.Center)
    )
}

interface AppFocusable {
    data object Entries: AppFocusable
    data object Editor: AppFocusable
}

@Composable
private fun App(windowWidth: Dp) {
    val overlays = rememberOverlayState()
    val appPointerListeners = LocalAppPointerListeners.current

    var pakLoaded by remember { mutableStateOf(false) }
    var selectedJsonSourcePath: String? by remember { mutableStateOf(null) }

    val states = remember { mutableStateMapOf<String, EditorState>() }

    var focusedArea by remember { mutableStateOf<AppFocusable>(AppFocusable.Entries) }

    val blur by animateFloatAsState(if (overlays.any()) 50f else 0f)

    val entriesWidth = 550.dp

    val slideRatio = 0.2f
    val containerOffset by animateDpAsState(
        targetValue =
            if (focusedArea == AppFocusable.Editor && selectedJsonSourcePath != null) (windowWidth * (-slideRatio / 2))
            else (windowWidth * (slideRatio / 2)),
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val entriesOffset by animateDpAsState(
        targetValue =
            if (focusedArea == AppFocusable.Editor && selectedJsonSourcePath != null) (windowWidth * slideRatio) - 12.dp
            else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val entriesBrightness by animateFloatAsState(
        targetValue =
            if (focusedArea == AppFocusable.Editor && selectedJsonSourcePath != null) 0.75f
            else 0f
    )

    var preview by remember { mutableStateOf<DungeonsJsonFile.Preview>(DungeonsJsonFile.Preview.None) }

    val onSelect: (DungeonsJsonState) -> Unit = {
        if (!states.containsKey(it.sourcePath)) {
            val file = DungeonsJsonFile(it.sourcePath)
            states[it.sourcePath] = EditorState(DungeonsJsonState(file.read(), file))
        }
        selectedJsonSourcePath = it.sourcePath
        focusedArea = AppFocusable.Editor
        preview = DungeonsJsonFile.Preview.None

        ArcticSave.updateRecentFiles(it.sourcePath)
    }

    LaunchedPakLoadEffect(
        overlays = overlays,
        onLoad = { pakLoaded = true }
    )

    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(color = Color.White),
        LocalOverlayState provides overlays,
        LocalScrollbarStyle provides GlobalScrollBarStyle
    ) {
        AppRoot(
            overlays = overlays,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xff272727))
                .onKeyEvent { if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) overlays.pop() else false }
                .then(appPointerListeners.onGlobalPointerEventModifier())
        ) {
            AnimatedVisibility(
                visible = pakLoaded,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { renderEffect = if (blur == 0f) null else BlurEffect(blur, blur) }
            ) {
                Image(
                    bitmap = DungeonsTextures["/Game/UI/Materials/LoadingScreens/Loading_Ancient_Hunt.png"],
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
                Row(
                    modifier = Modifier
                        .requiredWidth(windowWidth * (1 + slideRatio))
                        .offset { IntOffset(containerOffset.roundToPx(), 0) }
                ) {
                    JsonEntries(
                        onJsonSelect = { onSelect(it) },
                        preview = preview,
                        focused = focusedArea == AppFocusable.Entries || selectedJsonSourcePath == null,
                        requestFocus = { focusedArea = AppFocusable.Entries },
                        modifier = Modifier
                            .width(entriesWidth)
                            .offset { IntOffset(entriesOffset.roundToPx(), 0) }
                            .drawWithContent {
                                drawContent()
                                drawRect(Color.Black, alpha = entriesBrightness)
                            },
                    )
                    JsonEditor(
                        state = selectedJsonSourcePath?.let { states[it] },
                        modifier = Modifier
                            .weight(1f)
                            .clickable(rememberMutableInteractionSource(), null) {
                                focusedArea = AppFocusable.Editor
                            },
                        requestClose = { selectedJsonSourcePath = null },
                        placeholder = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.requiredWidth(windowWidth - entriesWidth)
                            ) {
                                FileSelector(
                                    validator = {
                                        preview = DungeonsJsonFile(it).preview()
                                        preview is DungeonsJsonFile.Preview.Valid
                                    },
                                    buttonText = Localizations.UiText("open"),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .requiredHeight(525.dp)
                                        .padding(start = 25.dp, end = 25.dp, top = 32.dp),
                                    onSelect = {
                                        preview.let {
                                            if (it is DungeonsJsonFile.Preview.Valid) {
                                                onSelect(it.json)
                                            } // FileSelector onSelect preview.let if block
                                        } // FileSelector onSelect preview.let lambda
   /* WOW! SO LOGICAL! */           } // FileSelector onSelect lambda
                                ) // FileSelector parameters
                            } // Column content lambda
                        } // JsonEditor placeholder lambda
                    ) // JsonEditor parameters
                } // Row content lambda
            } // AnimatedVisibility content lambda
        } // AppRoot content lambda
    } // CompositionLocalProvider content lambda
} // function App

@Composable
fun AppRoot(
    overlays: OverlayState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        content()
        overlays.Stack()
    }
}

@Composable
private fun LaunchedPakLoadEffect(overlays: OverlayState, onLoad: () -> Unit) {
    LaunchedEffect(true) {
        fun onPakLoaded(indexingOverlayId: String) {
            onLoad()
            overlays.destroy(indexingOverlayId)
        }

        fun onPakNotFound(indexingOverlayId: String) {
            fun onNewPakPathSelected(notFoundOverlayId: String, path: String) {
                LocalData.customPakLocation = path
                LocalData.save()

                overlays.destroy(notFoundOverlayId)
            }

            overlays.destroy(indexingOverlayId)
            overlays.make(canBeDismissed = false) { overlayId ->
                PakNotFoundOverlay(onSelect = { onNewPakPathSelected(overlayId, it) })
            }
        }

        overlays.make(
            canBeDismissed = false,
            backdropOptions = Overlay.BackdropOptions(alpha = 0.6f)
        ) { id ->
            PakIndexingOverlay(
                onSuccess = { onPakLoaded(id) },
                onFailure = { onPakNotFound(id) }
            )
        }
    }
}


private val GlobalScrollBarStyle =
    ScrollbarStyle(
        thickness = 20.dp,
        minimalHeight = 100.dp,
        hoverColor = Color.White.copy(alpha = 0.3f),
        unhoverColor = Color.White.copy(alpha = 0.15f),
        hoverDurationMillis = 0,
        shape = RoundedCornerShape(3.dp),
    )
