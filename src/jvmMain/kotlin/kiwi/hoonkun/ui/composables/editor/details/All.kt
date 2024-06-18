package kiwi.hoonkun.ui.composables.editor.details

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp


@Composable
fun ItemComparator(editor: EditorState) {
    val scroll = rememberScrollState()
    val adapter = rememberScrollbarAdapter(scroll)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            ItemDetail(
                item = editor.selection.primary,
                editor = editor
            )
            Spacer(modifier = Modifier.height(20.dp))
            ItemDetail(
                item = editor.selection.secondary,
                editor = editor
            )
        }
        VerticalScrollbar(
            adapter = adapter,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp)
        )
    }
}

@Composable
fun Tips() =
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().alpha(0.85f)
    ) {
        TipsTitle()
        for (i in 0 until 6) {
            Tip(Localizations.UiText("tips_$i"))
        }
    }

@Composable
private fun TipsTitle() =
    Text(
        text = Localizations.UiText("tips_title"),
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 30.dp)
    )

@Composable
private fun Tip(text: String) =
    Text(
        text = text,
        fontSize = 24.sp,
        modifier = Modifier.padding(vertical = 20.dp)
    )
