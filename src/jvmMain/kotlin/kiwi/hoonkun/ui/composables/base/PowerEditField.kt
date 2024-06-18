package kiwi.hoonkun.ui.composables.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import kiwi.hoonkun.utils.toFixed
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsTextures


@Composable
fun PowerEditField(
    power: Double,
    onPowerChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var value by remember(power) { mutableStateOf("${power.toFixed(3)}") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        PowerIcon()
        Spacer(modifier = Modifier.width(10.dp))
        Text(DungeonsLocalizations["/gearpower_POWER"]!!, fontSize = 25.sp, color = Color.White)
        Spacer(modifier = Modifier.width(15.dp))
        TextFieldValidatable(
            value = value,
            onValueChange = { value = it },
            validator = { it.toDoubleOrNull() != null },
            onSubmit = { onPowerChange(value.toDouble()) },
            textStyle = TextStyle(fontSize = 25.sp, color = Color.White),
            modifier = Modifier.offset(y = (-2).dp)
        )
    }
}

@Composable
private fun PowerIcon() =
    Image(
        bitmap = DungeonsTextures["/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png"],
        contentDescription = null,
        modifier = Modifier.size(30.dp)
    )