package arctic.ui.composables.atomic

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import arctic.ui.utils.rememberMutableInteractionSource
import arctic.ui.unit.dp
import arctic.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemRarityButton(text: String, enabled: Boolean = true, color: Color, onClick: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .height(38.dp)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary), enabled = enabled, onClick = onClick)
            .hoverable(interaction, enabled)
            .background(color, shape = RoundedCornerShape(6.dp))
            .drawBehind { drawInteractionBorder(hovered, false) }
            .padding(vertical = 4.dp, horizontal = 10.dp)
    ) {
        Text(text = text, fontSize = 20.sp, color = Color.White)
    }
}
