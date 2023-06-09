package arctic.ui.utils

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberMutableInteractionSource() = remember { MutableInteractionSource() }