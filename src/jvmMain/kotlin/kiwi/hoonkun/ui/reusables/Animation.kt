package kiwi.hoonkun.ui.reusables

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

fun <T>defaultTween() = tween<T>(250)

fun defaultFadeIn() = fadeIn(defaultTween())
fun defaultFadeOut() = fadeOut(defaultTween())

fun defaultSlideIn(initialOffset: (fullSize: IntSize) -> IntOffset) = slideIn(defaultTween(), initialOffset)
fun defaultSlideOut(targetOffset: (fullSize: IntSize) -> IntOffset) = slideOut(defaultTween(), targetOffset)