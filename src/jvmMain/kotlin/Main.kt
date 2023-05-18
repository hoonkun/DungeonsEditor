import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    AppRoot {

    }
}

@Composable
fun AppRoot(content: @Composable BoxScope.() -> Unit) =
    Box(modifier = Modifier.fillMaxSize().background(Color(0x272727)), content=content)

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
