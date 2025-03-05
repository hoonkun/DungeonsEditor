package kiwi.hoonkun.ui.composables.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonDpCornerRadius
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.composables.base.TextFieldValidatable
import kiwi.hoonkun.ui.composables.overlays.CloseFileConfirmOverlay
import kiwi.hoonkun.ui.composables.overlays.FileSaveCompleteOverlay
import kiwi.hoonkun.ui.composables.overlays.FileSaveOverlay
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.states.Overlay
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.states.extensions.withCurrencies
import minecraft.dungeons.values.DungeonsItem
import minecraft.dungeons.values.asInGameLevel
import minecraft.dungeons.values.toFixed
import minecraft.dungeons.values.truncate


@Composable
fun EditorBottomBar(
    editor: EditorState,
    requestClose: () -> Unit
) {
    val stored = remember(editor) { editor.data }

    val levelIcon = remember { DungeonsTextures["/UI/Materials/Character/STATS_LV_frame.png"] }

    var level by remember { mutableStateOf("${stored.playerLevel.toFixed(3)}") }
    var emerald by remember { mutableStateOf("${withCurrencies { stored.emerald }}") }
    var gold by remember { mutableStateOf("${withCurrencies { stored.gold }}") }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(85.dp)
            .background(Color(0xff191919))
            .padding(horizontal = 170.dp)
    ) {
        CurrencyField(
            value = level,
            onValueChange = { level = it },
            onSubmit = { stored.playerLevel = it.toDouble().asInGameLevel() },
            validator = { it.toDoubleOrNull() != null }
        ) {
            Box(contentAlignment = Alignment.Center) {
                CurrencyImage(levelIcon, 0.8f)
                Text(text = "LV.", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        CurrencyText(
            icon = "/UI/Materials/MissionSelectMap/inspector/gear/powericon.png",
            scale = 0.8f,
            value = "${stored.playerPower.truncate()}"
        )

        CurrencyField(
            icon = "/UI/Materials/Emeralds/emerald_indicator.png",
            iconScale = 0.7f,
            value = emerald,
            onValueChange = { emerald = it },
            onSubmit = { newValue -> withCurrencies { editor.data.emerald = newValue.toInt() } },
            validator = { it.toIntOrNull() != null }
        )

        CurrencyField(
            icon = "/UI/Materials/Currency/GoldIndicator.png",
            iconScale = 0.9f,
            value = gold,
            onValueChange = { gold = it },
            onSubmit = { newValue -> withCurrencies { editor.data.gold = newValue.toInt() } },
            validator = { it.toIntOrNull() != null }
        )

        CurrencyText(
            icon = "/UI/Materials/Inventory2/Salvage/enchant_icon.png",
            scale = 0.7f,
            value = "${stored.playerLevel.truncate() - stored.totalSpentEnchantmentPoints}",
            valid = stored.playerLevel.truncate() - stored.totalSpentEnchantmentPoints >= 0,
            width = 50.dp
        )

        Spacer(modifier = Modifier.weight(1f))

        InventoryButton {
            editor.isInTowerEditMode = false
            editor.view = DungeonsItem.Location.Inventory
        }
        StorageButton {
            editor.isInTowerEditMode = false
            editor.view = DungeonsItem.Location.Storage
        }
        TowerButton {
            editor.isInTowerEditMode = true
        }

        Spacer(modifier = Modifier.width(30.dp))

        SaveButton(editor)
        CloseFileButton(requestClose)
    }
}

@Composable
private fun TowerButton(
    onClick: () -> Unit
) {
    ToolbarIconRetroButton(
        color = Color(0xff366c75),
        iconPath = "/UI/Materials/MissionSelectMap/legend/Marker_TowerSavePoint.png",
        onClick = onClick
    )
}

@Composable
private fun InventoryButton(
    onClick: () -> Unit
) {
    ToolbarIconRetroButton(
        color = Color(0xff7d6136),
        iconPath = "/UI/Materials/Map/Pins/mapicon_chest.png",
        onClick = onClick
    )
}

@Composable
private fun StorageButton(
    onClick: () -> Unit
) {
    ToolbarIconRetroButton(
        color = Color(0xff55367d),
        iconPath = "/UI/Materials/Map/Pins/mapicon_chest.png",
        onClick = onClick
    )
}

@Composable
private fun CloseFileButton(
    onClick: () -> Unit
) {
    val overlays = LocalOverlayState.current

    ToolbarIconRetroButton(
        color = Color(0xff77726c),
        iconPath = "/UI/Materials/Map/close.png",
        iconModifier = Modifier.size(35.dp)
    ) {
        overlays.make(backdropOptions = Overlay.BackdropOptions(alpha = 0.6f)) {
            CloseFileConfirmOverlay(
                onConfirm = onClick,
                requestClose = it
            )
        }
    }
}

@Composable
private fun SaveButton(editor: EditorState) {
    val overlays = LocalOverlayState.current

    ToolbarIconRetroButton(
        color = Color(0xff3f8e4f),
        iconPath = "/UI/Materials/Portrait/friend_check_play.png",
        iconModifier = Modifier.size(22.dp)
    ) {
        overlays.make(backdropOptions = Overlay.BackdropOptions(alpha = 0.6f)) {
            FileSaveOverlay(
                editor = editor,
                postSave = { overlays.make(backdropOptions = Overlay.BackdropOptions(alpha = 0.6f)) { FileSaveCompleteOverlay() } },
                requestClose = it
            )
        }
    }
}

@Composable
private fun ToolbarIconRetroButton(
    color: Color,
    iconPath: String,
    iconModifier: Modifier = Modifier.size(32.dp),
    onClick: () -> Unit,
) {
    val bitmap = remember(iconPath) { DungeonsTextures[iconPath] }

    RetroButton(
        color = color,
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        modifier = Modifier.padding(start = 16.dp).size(48.dp),
        radius = RetroButtonDpCornerRadius(all = 4.dp),
        stroke = 3.dp,
        onClick = onClick,
        content =  { Image(bitmap = bitmap, contentDescription = null, modifier = iconModifier, filterQuality = FilterQuality.None) }
    )
}

@Composable
private fun CurrencyImage(image: ImageBitmap, scale: Float = 1f) =
    Image(image, null, modifier = Modifier.size(50.dp).scale(scale))

@Composable
private fun CurrencyText(
    icon: String,
    value: String,
    valid: Boolean = true,
    scale: Float = 1f,
    width: Dp = 100.dp
) {
    val bitmap = remember(icon) { DungeonsTextures[icon] }

    CurrencyImage(bitmap, scale)
    Spacer(modifier = Modifier.width(10.dp))
    Text(
        text = value,
        fontSize = 25.sp,
        color = if (valid) Color.White else Color(0xffff5e14),
        modifier = Modifier.width(width)
    )
    Spacer(modifier = Modifier.width(30.dp))
}

@Composable
private fun CurrencyField(
    icon: String,
    iconScale: Float = 1f,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    validator: (String) -> Boolean
) {
    val bitmap = remember(icon) { DungeonsTextures[icon] }
    CurrencyField(value, onValueChange, onSubmit, validator) { CurrencyImage(bitmap, iconScale) }
}

@Composable
private fun CurrencyField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    validator: (String) -> Boolean,
    icon: @Composable () -> Unit
) {
    icon()
    Spacer(modifier = Modifier.width(10.dp))
    TextFieldValidatable(
        value = value,
        onValueChange = onValueChange,
        onSubmit = onSubmit,
        validator = validator,
        textStyle = TextStyle(fontSize = 25.sp, color = Color.White),
        modifier = Modifier.requiredWidth(100.dp)
    )
    Spacer(modifier = Modifier.width(30.dp))
}

