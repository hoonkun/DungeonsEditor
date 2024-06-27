package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp


@Composable
fun OverlayTitleText(text: String) =
    Text(
        text = text,
        color = Color.White,
        fontSize = 32.sp
    )

@Composable
fun OverlayDescriptionText(text: String, fontSize: TextUnit = 24.sp) =
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.4f),
        fontSize = fontSize,
        textAlign = TextAlign.Center
    )

@Composable
fun OverlayTitleDescription(
    title: String,
    description: String? = null
) {
    OverlayTitleText(title)
    if (description == null) return

    Spacer(modifier = Modifier.height(20.dp))
    OverlayDescriptionText(description)
}

@Composable
fun OverlayRoot(content: @Composable ColumnScope.() -> Unit) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
        content = content
    )
