import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.JsonEditor
import kiwi.hoonkun.ui.composables.JsonEntries
import kiwi.hoonkun.ui.composables.base.FileSelector
import kiwi.hoonkun.ui.composables.base.overlays.PakIndexingOverlay
import kiwi.hoonkun.ui.composables.base.overlays.PakNotFoundOverlay
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.states.*
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.io.DungeonsSaveFile
import minecraft.dungeons.resources.DungeonsTextures


fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(1800.dp, 1400.dp),
        position = WindowPosition(Alignment.Center)
    )

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

@Composable
private fun App(windowWidth: Dp) {
    val arctic = rememberArcticState()
    val overlays = rememberOverlayState()

    val entriesInteractionSource = rememberMutableInteractionSource()
    val entriesHovered by entriesInteractionSource.collectIsHoveredAsState()

    val entriesWidth = 550.dp

    val slideRatio = 0.1f
    val containerOffset by animateDpAsState(
        targetValue =
            if (!entriesHovered && arctic.json != null) (windowWidth * (-slideRatio / 2))
            else (windowWidth * (slideRatio / 2))
    )
    val entriesOffset by animateDpAsState(
        targetValue =
            if (!entriesHovered && arctic.json != null) (windowWidth * slideRatio) - 12.dp
            else 0.dp
    )
    val entriesBrightness by animateFloatAsState(
        targetValue =
            if (!entriesHovered && arctic.json != null) 0.75f
            else 0f
    )

    var selectorValidatedResult by remember { mutableStateOf<DungeonsSaveFile.ValidateResult>(DungeonsSaveFile.ValidateResult.None) }
    val hasPreview by remember { derivedStateOf { selectorValidatedResult is DungeonsSaveFile.ValidateResult.Valid } }

    LaunchedPakLoadEffect(arctic, overlays)

    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(color = Color.White),
        LocalArcticState provides arctic,
        LocalOverlayState provides overlays
    ) {
        AppRoot(
            overlays = overlays,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xff272727))
                .onKeyEvent { if (it.key == Key.Escape) overlays.pop() else false }
        ) {
            AnimatedVisibility(
                visible = arctic.pakLoaded,
                modifier = Modifier.fillMaxSize()
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
                        onJsonSelect = { arctic.json = it },
                        hasPreview = { hasPreview },
                        preview = { none, invalid, valid ->
                            AnimatedContent(
                                targetState = selectorValidatedResult,
                                transitionSpec = { defaultFadeIn() togetherWith defaultFadeOut() using SizeTransform(clip = false) }
                            ) {
                                when (it) {
                                    is DungeonsSaveFile.ValidateResult.None -> none()
                                    is DungeonsSaveFile.ValidateResult.Invalid -> invalid()
                                    is DungeonsSaveFile.ValidateResult.Valid -> valid(it.json, it.summary)
                                }
                            }
                        },
                        modifier = Modifier
                            .width(entriesWidth)
                            .offset { IntOffset(entriesOffset.roundToPx(), 0) }
                            .hoverable(entriesInteractionSource)
                            .drawWithContent {
                                drawContent()
                                drawRect(Color.Black, alpha = entriesBrightness)
                            },
                    )
                    JsonEditor(
                        json = arctic.json,
                        modifier = Modifier
                            .weight(1f)
                            .hoverable(rememberMutableInteractionSource())
                        ,
                        placeholder = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.requiredWidth(windowWidth - entriesWidth)
                            ) {
                                FileSelector(
                                    validator = validator@{
                                        val validateResult = DungeonsSaveFile(it).validate()
                                        selectorValidatedResult = validateResult

                                        return@validator validateResult is DungeonsSaveFile.ValidateResult.Valid
                                    },
                                    buttonText = Localizations.UiText("open"),
                                    modifier = Modifier.requiredHeight(525.dp),
                                    onSelect = onSelect@{
                                        val result = selectorValidatedResult
                                        if (result !is DungeonsSaveFile.ValidateResult.Valid) return@onSelect
                                        arctic.json = result.json
   /* WOW! SO LOGICAL! */           } // FileSelector onSelect lambda
                                ) // FileSelector parameters
                            } // Column content lambda
                        } // JsonEditor placeholder lambda
                    ) // JsonEditor parameters
                } // Row content lambda
            } // AnimatedVisibility content lambda
        } // AppRoot content lambda
    } // AppProvider content lambda
} // function App

@Composable
fun AppRoot(
    overlays: OverlayState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        content()
        overlays.Stack(this)
    }
}

@Composable
private fun LaunchedPakLoadEffect(arctic: ArcticState, overlays: OverlayState) {
    LaunchedEffect(true) {
        fun onPakLoaded(indexingOverlayId: String) {
            arctic.pakLoaded = true
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
