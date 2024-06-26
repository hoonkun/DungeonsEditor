package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kiwi.hoonkun.ui.reusables.defaultTween
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate

@Composable
fun AutoHidingVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val adapter = rememberScrollbarAdapter(scrollState)

    var visible by remember { mutableStateOf(false) }
    val scrollbarAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = defaultTween()
    )

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .conflate()
            .collect {
                visible = true
                delay(1000)
                visible = false
            }
    }

    VerticalScrollbar(
        adapter = adapter,
        modifier = modifier
            .graphicsLayer { alpha = scrollbarAlpha }
    )
}