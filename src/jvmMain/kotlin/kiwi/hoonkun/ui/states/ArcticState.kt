package kiwi.hoonkun.ui.states

import androidx.compose.runtime.*

@Stable
class ArcticState {
    var pakLoaded by mutableStateOf(false)
    var path: String? by mutableStateOf(null)
}

@Composable
fun rememberArcticState() = remember { ArcticState() }