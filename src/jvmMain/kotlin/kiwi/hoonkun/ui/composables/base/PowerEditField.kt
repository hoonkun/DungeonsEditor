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
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.values.InGameDungeonsPower
import minecraft.dungeons.values.asInGamePower
import utils.toFixed


@Composable
fun PowerEditField(
    power: InGameDungeonsPower,
    onPowerChange: (InGameDungeonsPower) -> Unit,
    modifier: Modifier = Modifier,
    hideLabel: Boolean = false
) {
    var value by remember(power) { mutableStateOf("${power.value.toFixed(3)}") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        PowerIcon()
        if (!hideLabel) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(DungeonsLocalizations["/gearpower_POWER"]!!, fontSize = 25.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.width(15.dp))
        TextFieldValidatable(
            value = value,
            onValueChange = { value = it },
            validator = { it.toDoubleOrNull() != null },
            onSubmit = { onPowerChange(value.toDouble().asInGamePower()) },
            textStyle = TextStyle(fontSize = 25.sp, color = Color.White),
            modifier = Modifier.offset(y = (-2).dp).width(100.dp)
        )
    }
}

@Composable
fun PowerIcon(modifier: Modifier = Modifier.size(30.dp)) =
    Image(
        bitmap = DungeonsTextures["/UI/Materials/MissionSelectMap/inspector/gear/powericon.png"],
        contentDescription = null,
        modifier = modifier
    )
