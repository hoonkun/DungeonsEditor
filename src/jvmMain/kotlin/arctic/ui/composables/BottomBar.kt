package arctic.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import arctic.states.arctic
import dungeons.DungeonsLevel
import dungeons.IngameImages
import dungeons.states.DungeonsJsonState
import dungeons.states.extensions.playerLevel
import dungeons.states.extensions.playerPower
import dungeons.states.extensions.totalSpentEnchantmentPoints
import extensions.toFixed

@Composable
fun BottomBar(stored: DungeonsJsonState) {
    val emerald = stored.currencies.find { it.type == "Emerald" }
    val gold = stored.currencies.find { it.type == "Gold" }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff191919))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.725f)
        ) {
            CurrencyField(
                value = "${stored.playerLevel.toFixed(3)}",
                onValueChange = {
                    if (it.toDoubleOrNull() != null) stored.xp = DungeonsLevel.toSerializedLevel(it.toDouble())
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CurrencyImage(IngameImages.get { "/Game/UI/Materials/Character/STATS_LV_frame.png" })
                    Text(
                        text = "LV.",
                        style = TextStyle(fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            }

            CurrencyText(
                icon = "/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png",
                value = "${stored.playerPower}"
            )

            CurrencyField(
                icon = "/Game/UI/Materials/Emeralds/emerald_indicator.png",
                value = "${emerald?.count ?: 0}",
                onValueChange = { if (it.toIntOrNull() != null) emerald?.count = it.toInt() }
            )

            CurrencyField(
                icon = "/Game/UI/Materials/Currency/GoldIndicator.png",
                value = "${gold?.count ?: 0}",
                onValueChange = { if (it.toIntOrNull() != null) gold?.count = it.toInt() }
            )

            CurrencyText(
                icon = "/Game/UI/Materials/Inventory2/Salvage/enchant_icon.png",
                value = "${stored.playerLevel - stored.totalSpentEnchantmentPoints}",
                smallIcon = true
            )

            Spacer(modifier = Modifier.weight(1f))

            InventorySwitcher()

            Spacer(modifier = Modifier.width(20.dp))

            SaveButton()
            CloseFileButton()
        }
    }
}

@Composable
fun IconButton(icon: String, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    Image(
        bitmap = IngameImages.get { icon },
        contentDescription = null,
        modifier = Modifier
            .size(60.dp)
            .hoverable(source)
            .clickable(source, null, onClick = onClick)
            .drawBehind { if (hovered) drawRoundRect(Color.White, alpha = 0.15f, cornerRadius = CornerRadius(6.dp.value)) }
            .padding(10.dp)
    )
}

@Composable
fun CloseFileButton() = IconButton("/Game/UI/Materials/Map/Pins/dungeon_door.png") { arctic.alerts.closeFile = true }
@Composable
fun SaveButton() = IconButton("/Game/UI/Materials/Map/Pins/mapicon_chest.png") { arctic.dialogs.fileSaveDstSelector = true }

@Composable
fun InventorySwitcher() {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val pressed by source.collectIsPressedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .hoverable(source)
            .clickable(source, null) { arctic.toggleView() }
            .height(60.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.White,
                    alpha = if (pressed) 0.2f else if (hovered) 0.15f else 0f,
                    cornerRadius = CornerRadius(6.dp.value, 6.dp.value)
                )
            }
            .padding(start = 15.dp)
    ) {

        Box(modifier = Modifier.width(32.5.dp)) {
            Image(
                bitmap = IngameImages.get { "/Game/UI/Materials/Character/left_arrow_carousel.png" },
                contentDescription = null,
                modifier = Modifier.width(20.dp).align(Alignment.CenterStart)
            )
            Image(
                bitmap = IngameImages.get { "/Game/UI/Materials/Character/right_arrow_carousel.png" },
                contentDescription = null,
                modifier = Modifier.width(20.dp).align(Alignment.CenterEnd)
            )
        }

        Text(
            text = if (arctic.view == "inventory") "Inventory" else "Storage",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.fillMaxHeight().padding(vertical = 10.dp, horizontal = 15.dp).offset(y = 3.dp)
        )

    }
}

@Composable
private fun CurrencyText(icon: String, value: String, smallIcon: Boolean = false) {
    CurrencyImage(IngameImages.get { icon }, smallIcon)
    Spacer(modifier = Modifier.width(10.dp))
    Text(text = value, style = TextStyle(fontSize = 25.sp, color = Color.White), modifier = Modifier.width(100.dp))
    Spacer(modifier = Modifier.width(30.dp))
}

@Composable
private fun CurrencyField(icon: String, value: String, onValueChange: (String) -> Unit) {
    CurrencyImage(IngameImages.get { icon }, true)
    Spacer(modifier = Modifier.width(10.dp))
    CurrencyField(value = value, onValueChange = onValueChange)
    Spacer(modifier = Modifier.width(30.dp))
}

@Composable
private fun CurrencyField(value: String, onValueChange: (String) -> Unit, icon: @Composable () -> Unit) {
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
