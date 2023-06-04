package arctic.ui.unit

import Settings
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Int.ddp: Dp get() = this.dp * Settings.globalScale
val Float.ddp: Dp get() = this.dp * Settings.globalScale
val Double.ddp: Dp get() = this.dp * Settings.globalScale
val Int.dsp: TextUnit get() = this.sp * Settings.globalScale
val Float.dsp: TextUnit get() = this.sp * Settings.globalScale
val Double.dsp: TextUnit get() = this.sp * Settings.globalScale