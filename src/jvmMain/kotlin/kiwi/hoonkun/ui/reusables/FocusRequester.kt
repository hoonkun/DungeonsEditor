package kiwi.hoonkun.ui.reusables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester


@Composable
fun rememberFocusRequester() = remember { FocusRequester() }