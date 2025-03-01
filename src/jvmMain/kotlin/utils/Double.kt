package utils

import kotlin.math.pow
import kotlin.math.truncate

fun Double.toFixed(digits: Int) =
    10.0.pow(digits).let { multiplier -> truncate(times(multiplier)).div(multiplier) }