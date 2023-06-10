package arctic.ui.composables.atomic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.IngameImages
import dungeons.Localizations

@Composable
fun PowerEditField(value: String, containerModifier: Modifier = Modifier, inputModifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val lineColor by animateColorAsState(
        targetValue =
            if (!focused) Color(0xff888888)
            else Color(0xffff884c),
        animationSpec = tween(durationMillis = 250)
    )

    Row(verticalAlignment = Alignment.CenterVertically, modifier = containerModifier) {
        PowerIcon()
        Spacer(modifier = Modifier.width(10.dp))
        Row {
            Text(Localizations["/gearpower_POWER"]!!, fontSize = 25.sp, color = Color.White)
            Spacer(modifier = Modifier.width(15.dp))
            BasicTextField(
                value,
                onValueChange,
                textStyle = TextStyle(fontSize = 25.sp, color = Color.White),
                singleLine = true,
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .then(inputModifier)
                    .onFocusChanged { focused = it.hasFocus }
                    .drawBehind {
                        drawRect(lineColor, topLeft = Offset(0f, size.height), size = Size(size.width, densityDp(3)))
                    }
            )
        }
    }
}

@Composable
private fun PowerIcon() =
    Image(
        bitmap = IngameImages.get { "/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png" },
        contentDescription = null,
        modifier = Modifier.size(30.dp)
    )
