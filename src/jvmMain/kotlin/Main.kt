import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.zIndex
import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.Resources
import kiwi.hoonkun.ui.composables.JsonEditor
import kiwi.hoonkun.ui.composables.JsonEditorTabButton
import kiwi.hoonkun.ui.composables.JsonEntries
import kiwi.hoonkun.ui.composables.base.FileSelector
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.composables.overlays.*
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.reusables.round
import kiwi.hoonkun.ui.states.*
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.io.DungeonsJsonFile
import minecraft.dungeons.resources.DungeonsTextures
import kotlin.random.Random


fun main() = application {
    val windowSize = remember(0xC0FFEE.dp) { DpSize(1800.dp, 1400.dp) }

    val arcticWindowState = remember {
        ArcticWindowState(WindowState(size = windowSize, position = WindowPosition(Alignment.Center)))
    }

    SideEffect {
        arcticWindowState.parent.size = windowSize
        arcticWindowState.parent.position = WindowPosition(Alignment.Center)
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = arcticWindowState.parent,
        resizable = false,
        visible = arcticWindowState.visible,
        title = "Dungeons Editor",
        icon = Resources.Drawables.icon(),
    ) {
        App(arcticWindowState, ::exitApplication)
    }
}

@Composable
private fun App(windowState: ArcticWindowState, requestExit: () -> Unit) {
    val overlays = rememberOverlayState()
    val appPointerListeners = LocalAppPointerListeners.current

    val windowWidth = windowState.size.width

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
    val tabsOffset by animateDpAsState(
        targetValue = if (focusedArea == AppFocusable.Editor && selectedJsonSourcePath != null || states.size > 0) (-114).dp else 0.dp,
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

        ArcticSettings.updateRecentFiles(it.sourcePath)
    }

    val onTabSelect: (String?) -> Unit = {
        selectedJsonSourcePath = it
        focusedArea = if (it != null) AppFocusable.Editor else AppFocusable.Entries
    }

    LaunchedPakLoadEffect(
        overlays = overlays,
        onLoad = { pakLoaded = true },
        requestExit = requestExit
    )

    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 20.sp, color = Color.White),
        LocalOverlayState provides overlays,
        LocalScrollbarStyle provides GlobalScrollBarStyle,
        LocalWindowState provides windowState,
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
                        focused = focusedArea == AppFocusable.Entries,
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
                        requestClose = {
                            val path = selectedJsonSourcePath
                            selectedJsonSourcePath = null
                            states.remove(path)
                        },
                        tabs = {
                            Column(
                                modifier = Modifier
                                    .requiredWidth(114.dp)
                                    .padding(top = 24.dp, start = 10.dp)
                                    .offset { IntOffset(x = tabsOffset.roundToPx(), y = 0) }
                            ) {
                                JsonEditorTabButton(
                                    bitmap = DungeonsTextures["/Game/UI/Materials/Map/Pins/mapicon_chest.png"],
                                    selected = selectedJsonSourcePath == null,
                                    onClick = { onTabSelect(null) },
                                    contentPadding = PaddingValues(16.dp)
                                )
                                states.keys.forEach { key ->
                                    JsonEditorTabButton(
                                        bitmap = DungeonsTextures.pets[Random(key.hashCode()).nextInt(DungeonsTextures.pets.size)],
                                        selected = selectedJsonSourcePath == key,
                                        onClick = { onTabSelect(key) }
                                    )
                                }
                            }
                        },
                        placeholder = {
                            Box(
                                modifier = Modifier
                                    .requiredWidth(windowWidth - entriesWidth)
                                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                    .drawBehind {
                                        val image =
                                            DungeonsTextures["/Game/UI/Materials/LoadingScreens/loadingscreen_subdungeon.png"]
                                        val dstSize = Size(
                                            size.width,
                                            size.width * (image.height.toFloat() / image.width)
                                        ).round()
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
                                    MainMenuButtons(requestExit = requestExit)
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .zIndex(1f)
                                        .alpha(0.5f)
                                ) {
                                    Text(text = "Dungeons Editor, 1.1.0 by HoonKun", fontFamily = Resources.Fonts.JetbrainsMono)
                                    Text(text = "Compatible with Minecraft Dungeons 1.17.0.0", fontFamily = Resources.Fonts.JetbrainsMono)
                                }
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxSize()
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
                                            .padding(top = 32.dp)
                                            .offset(y = 110.dp),
                                        onSelect = {
                                            preview.let {
                                                if (it is DungeonsJsonFile.Preview.Valid) {
                                                    onSelect(it.json)
                                                } // FileSelector onSelect preview.let if block
                                            } // FileSelector onSelect preview.let lambda
  /* WOW! SO LOGICAL! */                } // FileSelector onSelect lambda
                                    ) // FileSelector parameters
                                } // Column content lambda
                            } // Box content lambda
                        } // JsonEditor placeholder lambda
                    ) // JsonEditor parameters
                } // Row content lambda
            } // AnimatedVisibility content lambda
        } // AppRoot content lambda
    } // CompositionLocalProvider content lambda
} // function App

@Composable
private fun AppRoot(
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
fun MainMenuButtons(
    description: String? = Localizations["exit_application_description"],
    requestExit: () -> Unit
) {
    val overlays = LocalOverlayState.current

    MainIconButton(
        bitmap = Resources.Drawables.leave,
        onClick = {
            overlays.make {
                ExitApplicationConfirmOverlay(
                    description = description,
                    onConfirm = requestExit,
                    requestClose = { overlays.destroy(it) }
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

@Composable
private fun LaunchedPakLoadEffect(
    overlays: OverlayState,
    onLoad: () -> Unit,
    requestExit: () -> Unit
) {
    LaunchedEffect(true) {
        fun onPakLoaded(indexingOverlayId: String) {
            onLoad()
            overlays.destroy(indexingOverlayId)
        }

        fun onPakNotFound(indexingOverlayId: String) {
            fun onNewPakPathSelected(notFoundOverlayId: String, path: String) {
                ArcticSettings.customPakLocation = path
                ArcticSettings.save()

                overlays.destroy(notFoundOverlayId)
            }

            overlays.destroy(indexingOverlayId)
            overlays.make(canBeDismissed = false) { overlayId ->
                PakNotFoundOverlay(
                    onSelect = { onNewPakPathSelected(overlayId, it) },
                    requestExit = requestExit
                )
            }
        }

        overlays.make(
            canBeDismissed = false,
            backdropOptions = Overlay.BackdropOptions(alpha = 0.6f)
        ) { id ->
            PakIndexingOverlay(
                onSuccess = { onPakLoaded(id) },
                onFailure = { onPakNotFound(id) },
                onError = { error ->
                    overlays.destroy(id)
                    overlays.make(canBeDismissed = false) {
                        ErrorOverlay(
                            e = error,
                            title = Localizations["error_pak_title"]
                        )
                    }
                }
            )
        }
    }
}

@Stable
class ArcticWindowState(initialState: WindowState) {
    var parent: WindowState by mutableStateOf(initialState)

    val isMinimized get() = parent.isMinimized
    val placement get() = parent.placement
    val position get() = parent.position
    val size get() = parent.size

    var visible by mutableStateOf(true)
}

val LocalWindowState = staticCompositionLocalOf {
    ArcticWindowState(
        WindowState(
            size = DpSize(1800.dp, 1400.dp),
            position = WindowPosition(Alignment.Center)
        )
    )
}

interface AppFocusable {
    data object Entries: AppFocusable
    data object Editor: AppFocusable
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
