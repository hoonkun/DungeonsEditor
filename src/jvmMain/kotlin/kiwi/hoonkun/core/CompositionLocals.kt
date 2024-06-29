package kiwi.hoonkun.core

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import kiwi.hoonkun.ui.states.AppState
import kiwi.hoonkun.ui.states.LocalAppState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp


@Composable
fun AppCompositionLocals(
    windowState: WindowState,
    scope: ApplicationScope? = null,
    content: @Composable () -> Unit
) {
    val textStyle = remember(0xC0FFEE.dp) {
        TextStyle(
            color = Color.White,
            fontSize = 20.sp,
        )
    }
    val scrollbarStyle = remember(0xC0FFEE.dp) {
        ScrollbarStyle(
            thickness = 16.dp,
            minimalHeight = 100.dp,
            hoverColor = Color.White.copy(alpha = 0.25f),
            unhoverColor = Color.White.copy(alpha = 0.1f),
            hoverDurationMillis = 0,
            shape = RoundedCornerShape(3.dp),
        )
    }

    val appState = remember { AppState(scope = scope) }

    CompositionLocalProvider(
        LocalTextStyle provides textStyle,
        LocalScrollbarStyle provides scrollbarStyle,
        LocalWindowState provides windowState,
        LocalAppState provides appState,
        content = content
    )
}

val LocalWindowState = staticCompositionLocalOf {
    WindowState(size = DpSize(1800.dp, 1400.dp), position = WindowPosition(Alignment.Center))
}
