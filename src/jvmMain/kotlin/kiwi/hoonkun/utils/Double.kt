package kiwi.hoonkun.utils

import kotlin.math.pow

fun Double.toFixed(digits: Int) = (this * 10.0.pow(digits)).toInt() / (10.0.pow(digits))