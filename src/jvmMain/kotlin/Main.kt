import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kiwi.hoonkun.ui.composables.Intro
import kiwi.hoonkun.ui.composables.base.overlays.PakIndexingOverlay
import kiwi.hoonkun.ui.composables.base.overlays.PakNotFoundOverlay
import kiwi.hoonkun.ui.states.*
import kiwi.hoonkun.ui.units.dp


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
        icon = painterResource("_icon.png")
    ) {
        App()
    }
}

@Composable
private fun App() {
    val arctic = rememberArcticState()
    val overlays = rememberOverlayState()

    LaunchedPakLoadEffect(arctic, overlays)

    AppProviders {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xff272727))
                .onKeyEvent { if (it.key == Key.Escape) overlays.pop() else false }
        ) {
            Intro(
                visible = arctic.pakLoaded && arctic.path == null,
                onPathSelect = { arctic.path = it }
            )
            overlays.Stack(this)
        }
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
            overlays.make { overlayId ->
                PakNotFoundOverlay(onSelect = { onNewPakPathSelected(overlayId, it) })
            }
        }

        overlays.make(Overlay.BackdropOptions(alpha = 0.6f)) { id ->
            PakIndexingOverlay(
                onSuccess = { onPakLoaded(id) },
                onFailure = { onPakNotFound(id) }
            )
        }
    }
}

@Composable
private fun AppProviders(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(color = Color.White),
        content = content
    )
}

