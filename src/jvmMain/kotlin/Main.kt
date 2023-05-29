import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import blackstone.states.ArcticStates
import blackstone.states.StoredDataState
import composable.BottomBar
import composable.Popups
import composable.inventory.InventoryView
import composable.inventory.StorageView
import io.StoredFile.Companion.readAsStoredFile
import java.io.File

val stored = StoredDataState(File(Constants.SaveDataFilePath).readAsStoredFile().root)
val arctic = ArcticStates(stored)

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App() {
    Debugging.recomposition("App")

    val backdropVisible =
        arctic.enchantments.hasDetailTarget ||
        arctic.armorProperties.hasDetailTarget ||
        arctic.armorProperties.hasCreateInto ||
        arctic.item.enabled != null ||
        arctic.popups.inventoryFull
    val moreBlur = arctic.item.target != null

    val popupBackdropBlurRadius by animateDpAsState(if (moreBlur) 100.dp else if (backdropVisible) 50.dp else 0.dp, tween(durationMillis = 250))

    AppRoot {
        MainContainer(popupBackdropBlurRadius) {
            AnimatedContent(
                targetState = arctic.view,
                transitionSpec = {
                    val enter = fadeIn(tween(durationMillis = 250)) + slideIn(tween(durationMillis = 250), initialOffset = { IntOffset(50, 0) })
                    val exit = fadeOut(tween(durationMillis = 250)) + slideOut(tween(durationMillis = 250), targetOffset = { IntOffset(-50, 0) })
                    enter with exit using SizeTransform(false) { _, _ -> tween(durationMillis = 250) }
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth(0.725f)) {
                        if (it == "inventory") InventoryView()
                        else StorageView()
                    }
                }
            }
            BottomBarContainer { BottomBar() }
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
        modifier = Modifier.weight(1f),
        content = content
    )

@Composable
fun BottomBarContainer(content: @Composable BoxScope.() -> Unit) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(85.dp)
            .background(Color(0xff191919)),
        content = content
    )

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(1800.dp, 1400.dp))
    Window(onCloseRequest = ::exitApplication, state = windowState, resizable = false) {
        App()
    }
}
