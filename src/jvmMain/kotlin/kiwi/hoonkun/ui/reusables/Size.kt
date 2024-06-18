package kiwi.hoonkun.ui.reusables

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

fun Size.round() = IntSize(width.roundToInt(), height.roundToInt())