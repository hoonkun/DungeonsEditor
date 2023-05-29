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
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import blackstone.states.ArcticStates
import blackstone.states.StoredDataState
import composable.BottomBar
import composable.Popups
import composable.inventory.InventoryView
import io.StoredFile.Companion.readAsStoredFile
import java.io.File

val stored = StoredDataState(File(Constants.SaveDataFilePath).readAsStoredFile().root)
val arctic = ArcticStates()

@Composable
@Preview
fun App() {
    Debugging.recomposition("App")

    val backdropVisible =
        arctic.enchantments.hasDetailTarget ||
        arctic.armorProperties.hasDetailTarget ||
        arctic.armorProperties.hasCreateInto ||
        arctic.creation.enabled ||
        arctic.popups.inventoryFull ||
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
        modifier = Modifier.weight(1f).fillMaxWidth(0.7638888f).offset(x = (-35).dp),
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
