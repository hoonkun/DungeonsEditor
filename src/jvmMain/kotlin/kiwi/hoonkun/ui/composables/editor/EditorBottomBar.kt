package kiwi.hoonkun.ui.composables.editor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.reusables.defaultTween
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.states.Currency
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.LocalArcticState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.values.DungeonsLevel


@Composable
fun EditorBottomBar(editor: EditorState) {
    val stored = remember(editor) { editor.stored }

    val emeraldHolder by remember(stored) { derivedStateOf { stored.currencies.find { it.type == "Emerald" } } }
    val goldHolder by remember(stored) { derivedStateOf { stored.currencies.find { it.type == "Gold" } } }

    val levelIcon = remember { DungeonsTextures["/Game/UI/Materials/Character/STATS_LV_frame.png"] }

    var level by remember { mutableStateOf("${stored.playerLevel}") }
    var emerald by remember { mutableStateOf("${emeraldHolder?.count ?: 0}") }
    var gold by remember { mutableStateOf("${goldHolder?.count ?: 0}") }


    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(85.dp)
            .background(Color(0xff191919))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 64.dp)
        ) {
            CurrencyField(
                value = level,
                onValueChange = { level = it },
                onSubmit = { stored.xp = DungeonsLevel.toSerializedLevel(it.toDouble()) },
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
                onSubmit = { newValue ->
                    emeraldHolder.let {
                        if (it == null)
                            stored.currencies.add(Currency("Emerald", newValue.toInt()))
                        else
                            it.count = newValue.toInt()
                    }
                },
                validator = { it.toIntOrNull() != null }
            )

            CurrencyField(
                icon = "/Game/UI/Materials/Currency/GoldIndicator.png",
                iconScale = 0.9f,
                value = gold,
                onValueChange = { gold = it },
                onSubmit = { newValue ->
                    goldHolder.let {
                        if (it == null)
                            stored.currencies.add(Currency("Gold", newValue.toInt()))
                        else
                            it.count = newValue.toInt()
                    }
                },
                validator = { it.toIntOrNull() != null }
            )

            CurrencyText(
                icon = "/Game/UI/Materials/Inventory2/Salvage/enchant_icon.png",
                scale = 0.7f,
                value = "${stored.playerLevel.toInt() - stored.totalSpentEnchantmentPoints}",
                valid = stored.playerLevel.toInt() - stored.totalSpentEnchantmentPoints >= 0,
            )

            Spacer(modifier = Modifier.weight(1f))

            InventorySwitcher(
                current = editor.view,
                onSwitch = { editor.view = it }
            )

            Spacer(modifier = Modifier.width(20.dp))

            SaveButton()
            CloseFileButton()
        }
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
private fun CloseFileButton() {
    // TODO
    val overlays = LocalArcticState.current
    IconButton("/Game/UI/Materials/Map/Pins/dungeon_door.png") {  }
}

@Composable
private fun SaveButton() {
    // TODO
    val overlays = LocalArcticState.current
    IconButton("/Game/UI/Materials/Map/Pins/mapicon_chest.png") {  }
}

@Composable
private fun InventorySwitcher(
    current: EditorState.EditorView,
    onSwitch: (EditorState.EditorView) -> Unit
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

        Text(
            text = current.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.fillMaxHeight().padding(vertical = 10.dp, horizontal = 15.dp).offset(y = 3.dp)
        )
    }
}

@Composable
private fun CurrencyImage(image: ImageBitmap, scale: Float = 1f) =
    Image(image, null, modifier = Modifier.size(50.dp).scale(scale))

@Composable
private fun CurrencyText(icon: String, value: String, valid: Boolean = true, scale: Float = 1f) {
    val bitmap = remember(icon) { DungeonsTextures[icon] }

    CurrencyImage(bitmap, scale)
    Spacer(modifier = Modifier.width(10.dp))
    Text(text = value, fontSize = 25.sp, color = if (valid) Color.White else Color(0xffff5e14), modifier = Modifier.width(100.dp))
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
    CurrencyFieldInput(value = value, onValueChange = onValueChange, onSubmit = onSubmit, validator = validator)
    Spacer(modifier = Modifier.width(30.dp))
}

@Composable
private fun CurrencyFieldInput(
    value: String,
    validator: (String) -> Boolean,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    val source = rememberMutableInteractionSource()
    val focused by source.collectIsFocusedAsState()
    val lineColor by animateColorAsState(
        targetValue = if (!focused) Color(0x00ff884c) else Color(0xffff884c),
        animationSpec = defaultTween()
    )

    val focusManager = LocalFocusManager.current

    val valid = remember(value, validator) { validator(value) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.requiredSize(100.dp, 36.dp)
    ) {
        BasicTextField(
            value,
            onValueChange,
            textStyle = TextStyle(fontSize = 25.sp, color = Color.White),
            singleLine = true,
            cursorBrush = SolidColor(Color.White),
            interactionSource = source,
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(lineColor, topLeft = Offset(0f, size.height), size = Size(size.width, 2.dp.toPx()))
                }
                .onKeyEvent {
                    if (it.key != Key.Enter) false
                    else if (!valid) false
                    else {
                        onSubmit(value)
                        focusManager.clearFocus()
                        true
                    }
                }
        )
        AnimatedContent(
            targetState = focused to valid,
            transitionSpec = {
                val enter = defaultFadeIn()
                val exit = defaultFadeOut()
                enter togetherWith exit using SizeTransform(clip = false)
            },
            modifier = Modifier
                .requiredSize(200.dp, 100.dp)
                .offset { IntOffset(0, -77.5.dp.roundToPx()) }
        ) { (capturedFocused, capturedValid) ->
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxSize()
            ) AnimatedBox@ {
                if (!capturedFocused) return@AnimatedBox

                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .drawBehind {
                            val color = if (capturedValid) Color(0xff2a3d2b) else Color(0xff5c3232)
                            val bottom = size.height - 10.dp.toPx()
                            drawRoundRect(
                                color = color,
                                cornerRadius = CornerRadius(6.dp.toPx()),
                                size = Size(size.width, bottom)
                            )

                            val triangleWidth = 10.dp.toPx()
                            val path = Path().apply {
                                moveTo((size.width - triangleWidth) / 2, bottom)
                                lineTo(size.width / 2, size.height)
                                lineTo((size.width + triangleWidth) / 2, bottom)
                            }
                            drawPath(path = path, color = color)
                        }
                        .padding(horizontal = if (capturedValid) 6.dp else 16.dp)
                        .padding(top = if (capturedValid) 6.dp else 12.dp, bottom = if (capturedValid) 16.dp else 22.dp)
                ) {
                    if (capturedValid) {
                        RetroButton(
                            text = "확정",
                            color = Color(0xff3f8e4f),
                            hoverInteraction = RetroButtonHoverInteraction.Outline,
                            modifier = Modifier.size(120.dp, 60.dp),
                            onClick = { onSubmit(value) }
                        )
                    } else {
                        Text(text = "잘못된 값이에요!")
                    }
                }
            }
        }
    }
}