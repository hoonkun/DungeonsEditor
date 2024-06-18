package arctic.ui.unit

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kiwi.hoonkun.ArcticSettings

val Int.ddp: Dp get() = this.dp * ArcticSettings.globalScale
val Float.ddp: Dp get() = this.dp * ArcticSettings.globalScale
val Double.ddp: Dp get() = this.dp * ArcticSettings.globalScale
val Int.dsp: TextUnit get() = this.sp * ArcticSettings.globalScale
val Float.dsp: TextUnit get() = this.sp * ArcticSettings.globalScale
val Double.dsp: TextUnit get() = this.sp * ArcticSettings.globalScale