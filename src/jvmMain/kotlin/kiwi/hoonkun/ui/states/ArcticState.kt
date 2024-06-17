package kiwi.hoonkun.ui.states

import androidx.compose.runtime.*

@Stable
class ArcticState {
    var pakLoaded by mutableStateOf(false)
    var json: DungeonsJsonState? by mutableStateOf(null)
}

val LocalArcticState = staticCompositionLocalOf<ArcticState> { throw RuntimeException("unreachable") }

@Composable
fun rememberArcticState() = remember { ArcticState() }