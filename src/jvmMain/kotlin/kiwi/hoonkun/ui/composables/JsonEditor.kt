package kiwi.hoonkun.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.reusables.defaultSlideIn
import kiwi.hoonkun.ui.reusables.defaultSlideOut
import kiwi.hoonkun.ui.states.DungeonsJsonState
import kiwi.hoonkun.ui.states.rememberEditorState
import kiwi.hoonkun.ui.units.dp

@Composable
fun JsonEditor(
    json: DungeonsJsonState?,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = { }
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .then(modifier)
            .background(Color(0xff202020))
    ) {
        AnimatedContent(
            targetState = json,
            transitionSpec = {
                val enter = defaultFadeIn() + defaultSlideIn { IntOffset(0, 20.dp.value.toInt()) }
                val exit = defaultFadeOut() + defaultSlideOut { IntOffset(0, -20.dp.value.toInt()) }
                enter togetherWith exit using SizeTransform(clip = false)
            },
            modifier = Modifier.fillMaxHeight()
        ) {
            if (it == null)
                placeholder()
            else
                JsonEditorContent(it)
        }
    }
}

@Composable
fun JsonEditorContent(json: DungeonsJsonState) {
    val editorState = rememberEditorState(json)

    Row(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}