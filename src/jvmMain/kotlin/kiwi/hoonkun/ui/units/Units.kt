package kiwi.hoonkun.ui.units

import Settings
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp as odp
import androidx.compose.ui.unit.sp as osp

val Int.dp: Dp get() = this.odp * Settings.globalScale
val Float.dp: Dp get() = this.odp * Settings.globalScale
val Double.dp: Dp get() = this.odp * Settings.globalScale
val Int.sp: TextUnit get() = this.osp * Settings.globalScale
val Float.sp: TextUnit get() = this.osp * Settings.globalScale
val Double.sp: TextUnit get() = this.osp * Settings.globalScale
