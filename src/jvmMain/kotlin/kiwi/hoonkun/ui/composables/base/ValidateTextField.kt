package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.units.dp


@Composable
fun TextFieldValidatable(
    value: String,
    onValueChange: (String) -> Unit,
    validator: (String) -> Boolean,
    onSubmit: (String) -> Unit,
    textStyle: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier,
    direction: PopupDirection = PopupDirection.Top,
    hideDecorationIfNotFocused: Boolean = false
) {
    val source = rememberMutableInteractionSource()
    val focused by source.collectIsFocusedAsState()
    val lineColor by minimizableAnimateColorAsState(
        targetValue =
            if (!focused) Color(if (hideDecorationIfNotFocused) 0x00ff884c else 0xff676767)
            else Color(0xffff884c),
        animationSpec = minimizableSpec { defaultTween() }
    )

    val focusManager = LocalFocusManager.current

    val valid = remember(value, validator) { validator(value) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.requiredHeight(36.dp).zIndex(1f)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            singleLine = true,
            cursorBrush = SolidColor(Color.White),
            interactionSource = source,
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(lineColor, topLeft = Offset(0f, size.height), size = Size(size.width, 2.dp.toPx()))
                }
                .onKeyEvent {
                    if (it.key == Key.Escape) {
                        focusManager.clearFocus()
                        true
                    } else if (it.key != Key.Enter) {
                        false
                    } else if (!valid) {
                        false
                    } else {
                        onSubmit(value)
                        focusManager.clearFocus()
                        true
                    }
                }
        )
        MinimizableAnimatedContent(
            targetState = focused to valid,
            transitionSpec = minimizableContentTransform spec@ {
                defaultFadeIn() togetherWith defaultFadeOut() using SizeTransform(clip = false)
            },
            modifier = Modifier
                .requiredSize(200.dp, 100.dp)
                .offset { IntOffset(0, if (direction == PopupDirection.Top) -77.5.dp.roundToPx() else 77.5.dp.roundToPx()) }
        ) { (capturedFocused, capturedValid) ->
            Box(
                contentAlignment =
                    if (direction == PopupDirection.Top) Alignment.BottomCenter
                    else Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            ) AnimatedBox@ {
                if (!capturedFocused) return@AnimatedBox

                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .drawBehind {
                            val color = if (capturedValid) Color(0xff2a3d2b) else Color(0xff5c3232)
                            val triangleWidth = 10.dp.toPx()
                            val margin = 10.dp.toPx()
                            val radius = CornerRadius(6.dp.toPx())
                            val rectSize = Size(size.width, size.height - margin)

                            if (direction == PopupDirection.Top) {
                                val bottom = size.height - margin
                                drawRoundRect(
                                    color = color,
                                    cornerRadius = radius,
                                    size = rectSize
                                )

                                val path = Path().apply {
                                    moveTo((size.width - triangleWidth) / 2, bottom)
                                    lineTo(size.width / 2, size.height)
                                    lineTo((size.width + triangleWidth) / 2, bottom)
                                }
                                drawPath(path = path, color = color)
                            } else {
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(0f, margin),
                                    size = rectSize,
                                    cornerRadius = radius,
                                )

                                val path = Path().apply {
                                    moveTo((size.width - triangleWidth) / 2, margin)
                                    lineTo(size.width / 2, 0f)
                                    lineTo((size.width + triangleWidth) / 2, margin)
                                }
                                drawPath(path = path, color = color)
                            }
                        }
                        .padding(horizontal = if (capturedValid) 6.dp else 16.dp)
                        .padding(top = if (capturedValid) 6.dp else 12.dp, bottom = if (capturedValid) 16.dp else 22.dp)

                ) {
                    val offsetModifier = Modifier.offset { IntOffset(0, if (direction == PopupDirection.Bottom) 10.dp.roundToPx() else 0) }
                    if (capturedValid) {
                        RetroButton(
                            text = Localizations["confirm_input"],
                            color = Color(0xff3f8e4f),
                            hoverInteraction = RetroButtonHoverInteraction.Outline,
                            modifier = Modifier.size(110.dp, 50.dp).then(offsetModifier),
                            onClick = { onSubmit(value) },
                        )
                    } else {
                        Text(text = Localizations["invalid_value"], modifier = offsetModifier)
                    }
                }
            }
        }
    }
}

enum class PopupDirection {
    Top, Bottom
}
