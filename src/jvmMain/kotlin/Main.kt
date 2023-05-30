import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import blackstone.states.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.*
import blackstone.states.ArcticStates
import blackstone.states.StoredDataState
import composable.BottomBar
import composable.Popups
import composable.Selector
import composable.inventory.InventoryView
import io.StoredFile.Companion.readAsStoredFile

val arctic = ArcticStates()

@Composable
@Preview
fun App() {
    Debugging.recomposition("App")

    val backdropVisible =
        arctic.dialogs.fileSaveDstSelector ||
        arctic.enchantments.hasDetailTarget ||
        arctic.armorProperties.hasDetailTarget ||
        arctic.armorProperties.hasCreateInto ||
        arctic.creation.enabled ||
        arctic.alerts.inventoryFull ||
        arctic.edition.target != null ||
        arctic.deletion.target != null ||
        arctic.duplication.target != null
    val moreBlur = arctic.creation.target != null

    val popupBackdropBlurRadius by animateDpAsState(if (moreBlur) 100.dp else if (backdropVisible) 50.dp else 0.dp, tween(durationMillis = 250))

    AppRoot {
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
                        arctic.stored = StoredDataState(it.readAsStoredFile().root)
                    } catch (e: Exception) {
                        arctic.alerts.fileLoadFailed = true
                    } finally {
                        arctic.dialogs.fileLoadSrcSelector = false
                    }
                }
            )
        }
    }
}
