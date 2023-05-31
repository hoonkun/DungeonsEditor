import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.*
import blackstone.states.*
import composable.BottomBar
import composable.Popups
import composable.RetroButton
import composable.Selector
import composable.inventory.InventoryView
import extensions.GameResources
import io.StoredFile.Companion.readAsStoredFile
import utils.openURL

@Composable
@Preview
fun App() {
    Debugging.recomposition("App")

    val backdropVisible = arctic.backdropBlur
    val moreBlur = arctic.creation.target != null

    val popupBackdropBlurRadius by animateDpAsState(if (moreBlur) 100.dp else if (backdropVisible) 50.dp else 0.dp, tween(durationMillis = 250))

    AppRoot {
        Background()
        MainContainer(popupBackdropBlurRadius) {
            ContentContainer {
                InventoryView()
            }
            BottomBarContainer { stored -> BottomBar(stored) }
        }
        Popups()
    }
}

@Composable
fun AppRoot(content: @Composable BoxScope.() -> Unit) =
    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff272727)),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Background() =
    AnimatedContent(
        targetState = arctic.stored == null,
        transitionSpec = { fadeIn() with fadeOut() },
        modifier = Modifier.fillMaxSize()
    ) { isInTitle ->
        if (isInTitle) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    bitmap = GameResources.image { "/Game/UI/Materials/LoadingScreens/Loading_Ancient_Hunt.png" },
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(50.dp)
                        .drawWithContent {
                            drawContent()
                            drawRect(Color.Black.copy(alpha = 0.6f))
                        }
                )
                RetroButton(
                    text = if (arctic.stored == null) "파일 선택" else "다른 파일 선택",
                    color = Color(0xff3f8e4f),
                    hoverInteraction = "outline"
                ) {
                    arctic.dialogs.fileLoadSrcSelector = true
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }

@Composable
fun BoxScope.MainContainer(blurRadius: Dp, content: @Composable ColumnScope.() -> Unit) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().blur(blurRadius),
        content = content
    )

@Composable
fun ColumnScope.ContentContainer(content: @Composable RowScope.() -> Unit) =
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.weight(1f).fillMaxWidth(0.7638888f).offset(x = (-35).dp),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomBarContainer(content: @Composable BoxScope.(StoredDataState) -> Unit) =
    AnimatedContent(
        targetState = arctic.stored,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(85.dp),
                    content = { content(it) }
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth())
            }
        }
    )

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(1800.dp, 1400.dp), position = WindowPosition(Alignment.Center)),
        resizable = false
    ) {
        App()
    }
    if (arctic.dialogs.fileSaveDstSelector) {
        Window(onCloseRequest = { arctic.dialogs.fileSaveDstSelector = false }, state = rememberWindowState(size = DpSize(1050.dp, 620.dp)), resizable = false) {
            Selector(
                selectText = "저장",
                validator = { !it.isDirectory },
                onSelect = { arctic.requireStored.save(it); arctic.dialogs.fileSaveDstSelector = false }
            )
        }
    }
    if (arctic.dialogs.fileLoadSrcSelector) {
        Window(onCloseRequest = { arctic.dialogs.fileLoadSrcSelector = false }, state = rememberWindowState(size = DpSize(1050.dp, 620.dp)), resizable = false) {
            Selector(
                selectText = "열기",
                validator = { !it.isDirectory && it.isFile },
                onSelect = {
                    try {
                        arctic.view = "inventory"
                        arctic.stored = StoredDataState(it.readAsStoredFile().root)
                    } catch (e: Exception) {
                        arctic.alerts.fileLoadFailed = e.message
                    } finally {
                        arctic.dialogs.fileLoadSrcSelector = false
                    }
                }
            )
        }
    }
}
