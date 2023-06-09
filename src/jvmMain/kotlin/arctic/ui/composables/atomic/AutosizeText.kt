package arctic.ui.composables.atomic

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun AutosizeText(
    text: String,
    maxFontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily = FontFamily.Default,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
    widthKey: Dp? = null,
    modifier: Modifier = Modifier
) {
    var fontSize by remember(widthKey) { mutableStateOf(maxFontSize) }
    var ready by remember(widthKey) { mutableStateOf(false) }

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        style = style,
        color = color,
        maxLines = 1,
        softWrap = false,
        modifier = modifier.drawWithContent { if (ready) drawContent() },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) fontSize *= 0.9
            else ready = true
        }
    )
}