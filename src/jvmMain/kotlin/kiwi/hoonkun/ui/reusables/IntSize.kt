package kiwi.hoonkun.ui.reusables

import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

operator fun IntSize.times(scale: Float): IntSize =
    IntSize((width * scale).roundToInt(), (height * scale).roundToInt())