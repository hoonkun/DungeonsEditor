package kiwi.hoonkun.ui.reusables

import androidx.compose.runtime.Composable


@Composable
inline fun <S, T>S.IfNotNull(value: T?, content: @Composable S.(T) -> Unit) {
    if (value == null) return
    content(value)
}

@Composable
inline fun <T>IfNotNull(value: T?, content: @Composable (T) -> Unit) {
    if (value == null) return
    content(value)
}
