package composable

import Debugging
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blackstone.states.common.common
import extensions.DungeonsLevel
import extensions.GameResources
import extensions.toFixed
import stored

@Composable
fun BottomBar() {
    Debugging.recomposition("BottomBar")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(0.725f)
    ) {
        CurrencyField(
            value = "${DungeonsLevel.toInGameLevel(stored.xp).toFixed(3)}",
            onValueChange = { if (it.toDoubleOrNull() != null) stored.xp = DungeonsLevel.toSerializedLevel(it.toDouble()) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                CurrencyImage(GameResources.image { "/Game/UI/Materials/Character/STATS_LV_frame.png" })
                Text(text = "LV.", style = TextStyle(fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold))
            }
        }

        CurrencyText(
            icon = "/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png",
            value = "${stored.common.power}"
        )

        CurrencyField(
            icon = "/Game/UI/Materials/Emeralds/emerald_indicator.png",
            value = "${stored.common.emeralds}",
            onValueChange = { if (it.toIntOrNull() != null) stored.currencies.find { currency -> currency.type == "Emerald" }?.count = it.toInt() }
        )

        CurrencyField(
            icon = "/Game/UI/Materials/Currency/GoldIndicator.png",
            value = "${stored.common.golds}",
            onValueChange = { if (it.toIntOrNull() != null) stored.currencies.find { currency -> currency.type == "Gold" }?.count = it.toInt() }
        )

        CurrencyText(
            icon = "/Game/UI/Materials/Inventory2/Salvage/enchant_icon.png",
            value = "${DungeonsLevel.toInGameLevel(stored.xp).toInt() -  stored.items.sumOf { it.enchantments?.sumOf { en -> en.investedPoints } ?: 0 }}",
            smallIcon = true
        )

        CurrencyField(
            icon = "/Game/UI/Materials/Currency/T_EyeOfEnder_Currency.png",
            value = "${stored.common.eyeOfEnder}",
            onValueChange = { if (it.toIntOrNull() != null) stored.currencies.find { currency -> currency.type == "EyeOfEnder" }?.count = it.toInt() }
        )
    }
}

@Composable
private fun CurrencyText(icon: String, value: String, smallIcon: Boolean = false) {
    Debugging.recomposition("CurrencyText")

    CurrencyImage(GameResources.image { icon }, smallIcon)
    Spacer(modifier = Modifier.width(10.dp))
    Text(text = value, style = TextStyle(fontSize = 25.sp, color = Color.White), modifier = Modifier.width(100.dp))
    Spacer(modifier = Modifier.width(30.dp))
}

@Composable
private fun CurrencyField(icon: String, value: String, onValueChange: (String) -> Unit) {
    Debugging.recomposition("CurrencyField(String, String, (String) -> Unit)")

    CurrencyImage(GameResources.image { icon }, true)
    Spacer(modifier = Modifier.width(10.dp))
    CurrencyField(value = value, onValueChange = onValueChange)
    Spacer(modifier = Modifier.width(30.dp))
}

@Composable
private fun CurrencyField(value: String, onValueChange: (String) -> Unit, icon: @Composable () -> Unit) {
    Debugging.recomposition("CurrencyField(String, (String) -> Unit, @Composable () -> Unit)")

    icon()
    Spacer(modifier = Modifier.width(10.dp))
    CurrencyField(value = value, onValueChange = onValueChange)
    Spacer(modifier = Modifier.width(30.dp))
}

@Composable
private fun CurrencyImage(image: ImageBitmap, small: Boolean = false) =
    Image(image, null, modifier = Modifier.size(if (small) 40.dp else 50.dp))

@Composable
private fun CurrencyField(value: String, onValueChange: (String) -> Unit) {
    Debugging.recomposition("CurrencyField(String, (String) -> Unit)")

    var focused by remember { mutableStateOf(false) }
    val lineColor by animateColorAsState(
        if (!focused) Color(0x00888888) else Color(0xffff884c),
        animationSpec = tween(durationMillis = 250)
    )

    BasicTextField(
        value,
        onValueChange,
        textStyle = TextStyle(fontSize = 25.sp, color = Color.White),
        singleLine = true,
        cursorBrush = SolidColor(Color.White),
        modifier = Modifier
            .onFocusChanged { focused = it.hasFocus }
            .width(100.dp)
            .drawBehind {
                drawRect(lineColor, topLeft = Offset(0f, size.height), size = Size(size.width, 3.dp.value))
            }
    )
}
