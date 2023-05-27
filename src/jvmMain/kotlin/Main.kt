import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import blackstone.states.ArcticStates
import blackstone.states.StoredDataState
import composable.BottomBar
import composable.PopupBox
import composable.inventory.InventoryView
import io.StoredFile.Companion.readAsStoredFile
import java.io.File

val stored = StoredDataState(File(Constants.SaveDataFilePath).readAsStoredFile().root)
val arctic = ArcticStates(stored)

@Composable
@Preview
fun App() {
    Debugging.recomposition("App")
    AppRoot {
        MainContainer {
            ContentContainer {
                InventoryView()
            }
            PopupBox()
        }
        BottomBarContainer { BottomBar() }
    }
}

@Composable
fun AppRoot(content: @Composable ColumnScope.() -> Unit) =
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff272727)),
        content = content
    )

@Composable
fun ColumnScope.MainContainer(content: @Composable BoxScope.() -> Unit) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().weight(1f),
        content = content
    )

@Composable
fun ContentContainer(content: @Composable RowScope.() -> Unit) =
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth(0.725f)
            .fillMaxHeight(),
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
