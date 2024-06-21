package kiwi.hoonkun.ui.reusables

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

fun <T>defaultTween() = tween<T>(250)

fun defaultFadeIn() = fadeIn(defaultTween())
fun defaultFadeOut() = fadeOut(defaultTween())