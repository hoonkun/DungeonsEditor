package blackstone.states

import Settings
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


val Int.ddp: State<Dp> get() = derivedStateOf { this.dp * Settings.globalScale }
val Float.ddp: State<Dp> get() = derivedStateOf { this.dp * Settings.globalScale }
val Double.ddp: State<Dp> get() = derivedStateOf { this.dp * Settings.globalScale }

val Int.dsp: State<TextUnit> get() = derivedStateOf { this.sp * Settings.globalScale }
val Float.dsp: State<TextUnit> get() = derivedStateOf { this.sp * Settings.globalScale }
val Double.dsp: State<TextUnit> get() = derivedStateOf { this.sp * Settings.globalScale }
