package kiwi.hoonkun.ui.composables.editor.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.AutoHidingVerticalScrollbar
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp


@Composable
fun ItemComparator(editor: EditorState) {
    val scroll = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(vertical = 24.dp)
        ) {
            ItemDetail(
                item = editor.primary,
                editor = editor
            )
            Spacer(modifier = Modifier.height(20.dp))
            ItemDetail(
                item = editor.secondary,
                editor = editor
            )
        }
        AutoHidingVerticalScrollbar(
            scrollState = scroll,
            modifier = Modifier
                .align(Alignment.TopEnd)
        )
    }
}

@Composable
fun Tips() =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().alpha(0.85f)
    ) {
        Text(
            text = Localizations["tip"],
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 24.sp
            )
        )
        Text(
            text = Localizations["tip_desc"],
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 24.sp
            ),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
