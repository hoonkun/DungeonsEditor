package kiwi.hoonkun.ui.composables.editor

import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import kiwi.hoonkun.ui.composables.base.TextFieldValidatable
import kiwi.hoonkun.ui.composables.overlays.CloseFileConfirmOverlay
import kiwi.hoonkun.ui.composables.overlays.FileSaveCompleteOverlay
import kiwi.hoonkun.ui.composables.overlays.FileSaveOverlay
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.DungeonsJsonEditorState
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.states.Overlay
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.states.extensions.withCurrencies


@Composable
fun EditorBottomBar(
    editor: DungeonsJsonEditorState,
    requestClose: () -> Unit
) {
    val stored = remember(editor) { editor.stored }

    val levelIcon = remember { DungeonsTextures["/Game/UI/Materials/Character/STATS_LV_frame.png"] }

    var level by remember { mutableStateOf("${stored.playerLevel}") }
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
            onSubmit = { stored.playerLevel = it.toDouble() },
            validator = { it.toDoubleOrNull() != null }
        ) {
            Box(contentAlignment = Alignment.Center) {
                CurrencyImage(levelIcon, 0.8f)
                Text(text = "LV.", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        CurrencyText(
            icon = "/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png",
            scale = 0.8f,
            value = "${stored.playerPower}"
        )

        CurrencyField(
            icon = "/Game/UI/Materials/Emeralds/emerald_indicator.png",
            iconScale = 0.7f,
            value = emerald,
            onValueChange = { emerald = it },
            onSubmit = { newValue -> withCurrencies { editor.stored.emerald = newValue.toInt() } },
            validator = { it.toIntOrNull() != null }
        )

        CurrencyField(
            icon = "/Game/UI/Materials/Currency/GoldIndicator.png",
            iconScale = 0.9f,
            value = gold,
            onValueChange = { gold = it },
            onSubmit = { newValue -> withCurrencies { editor.stored.gold = newValue.toInt() } },
            validator = { it.toIntOrNull() != null }
        )

        CurrencyText(
            icon = "/Game/UI/Materials/Inventory2/Salvage/enchant_icon.png",
            scale = 0.7f,
            value = "${stored.playerLevel.toInt() - stored.totalSpentEnchantmentPoints}",
            valid = stored.playerLevel.toInt() - stored.totalSpentEnchantmentPoints >= 0,
            width = 50.dp
        )

        Spacer(modifier = Modifier.weight(1f))

        InventorySwitcher(
            current = editor.view,
            onSwitch = { editor.view = it }
        )

        Spacer(modifier = Modifier.width(20.dp))

        SaveButton(editor)
        CloseFileButton(requestClose)
    }
}

@Composable
private fun IconButton(icon: String, onClick: () -> Unit) {
    val source = rememberMutableInteractionSource()
    val hovered by source.collectIsHoveredAsState()

    val bitmap = remember(icon) { DungeonsTextures[icon] }

    Image(
        bitmap = bitmap,
        contentDescription = null,
        modifier = Modifier
            .size(60.dp)
            .hoverable(source)
            .clickable(source, null, onClick = onClick)
            .drawBehind { if (hovered) drawRoundRect(Color.White, alpha = 0.15f, cornerRadius = CornerRadius(6.dp.toPx())) }
            .padding(10.dp)
    )
}

@Composable
private fun CloseFileButton(
    onClick: () -> Unit
) {
    val overlays = LocalOverlayState.current
    IconButton("/Game/UI/Materials/Map/Pins/dungeon_door.png") {
        overlays.make(backdropOptions = Overlay.BackdropOptions(alpha = 0.6f)) {
            CloseFileConfirmOverlay(
                onConfirm = onClick,
                requestClose = it
            )
        }
    }
}

@Composable
private fun SaveButton(editor: DungeonsJsonEditorState) {
    val overlays = LocalOverlayState.current
    IconButton("/Game/UI/Materials/Map/Pins/mapicon_chest.png") {
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
private fun InventorySwitcher(
    current: DungeonsJsonEditorState.EditorView,
    onSwitch: (DungeonsJsonEditorState.EditorView) -> Unit
) {
    val source = rememberMutableInteractionSource()
    val hovered by source.collectIsHoveredAsState()
    val pressed by source.collectIsPressedAsState()

    val leftArrow = remember { DungeonsTextures["/Game/UI/Materials/Character/left_arrow_carousel.png"] }
    val rightArrow = remember { DungeonsTextures["/Game/UI/Materials/Character/right_arrow_carousel.png"] }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .hoverable(source)
            .clickable(source, null) { onSwitch(current.other()) }
            .height(60.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.White,
                    alpha = if (pressed) 0.2f else if (hovered) 0.15f else 0f,
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
            .padding(start = 15.dp)
    ) {
        Box(modifier = Modifier.width(32.5.dp)) {
            Image(
                bitmap = leftArrow,
                contentDescription = null,
                modifier = Modifier.width(20.dp).align(Alignment.CenterStart)
            )
            Image(
                bitmap = rightArrow,
                contentDescription = null,
                modifier = Modifier.width(20.dp).align(Alignment.CenterEnd)
            )
        }

        MinimizableAnimatedContent(
            targetState = current,
            transitionSpec = minimizableContentTransform spec@ {
                val enter = defaultFadeIn()
                val exit = defaultFadeOut()
                enter togetherWith exit using SizeTransform(false)
            }
        ) { selected ->
            Text(
                text = selected.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 15.dp)
            )
        }
    }
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

