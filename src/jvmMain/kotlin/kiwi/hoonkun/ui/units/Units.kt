package kiwi.hoonkun.ui.units

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import kiwi.hoonkun.ArcticSettings
import androidx.compose.ui.unit.dp as odp
import androidx.compose.ui.unit.sp as osp

val Int.dp: Dp get() = this.odp * ArcticSettings.globalScale
val Float.dp: Dp get() = this.odp * ArcticSettings.globalScale
val Double.dp: Dp get() = this.odp * ArcticSettings.globalScale
val Int.sp: TextUnit get() = this.osp * ArcticSettings.globalScale
val Float.sp: TextUnit get() = this.osp * ArcticSettings.globalScale
val Double.sp: TextUnit get() = this.osp * ArcticSettings.globalScale
