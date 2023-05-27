package composable.blackstone.popup

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blackstone.states.ArmorProperty
import blackstone.states.items.ArmorPropertyRarityIcon
import blackstone.states.items.data

@Composable
fun ArmorPropertyDetail(property: ArmorProperty) {
    Debugging.recomposition("ArmorPropertyDetailView")

    Column(modifier = Modifier.width(675.dp).offset(x = 15.dp).background(Color(0xff080808)).padding(30.dp)) {
        Text(text = "방어구 속성", style = TextStyle(fontSize = 18.sp, color = Color.White))
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ArmorPropertyRarityToggle(property)
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = property.data.description!!,
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArmorPropertyRarityToggle(property: ArmorProperty) {
    Debugging.recomposition("ArmorPropertyRarityToggle")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    Image(
        bitmap = ArmorPropertyRarityIcon(property.rarity),
        contentDescription = null,
        modifier = Modifier
            .size(41.dp)
            .offset(y = 1.5.dp)
            .hoverable(source)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) { property.rarity = if (property.rarity.lowercase() == "common") "Unique" else "Common" }
            .drawBehind {
                if (hovered)
                    drawRoundRect(
                        SolidColor(Color.White.copy(0.3f)),
                        cornerRadius = CornerRadius(6.dp.value, 6.dp.value),
                        style = Stroke(width = 4.dp.value)
                    )
            }
            .padding(3.dp)
    )
}