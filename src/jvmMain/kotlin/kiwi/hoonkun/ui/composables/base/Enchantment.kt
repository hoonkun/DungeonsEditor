package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.ui.reusables.drawEnchantmentIconBorder
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.reusables.round
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.resources.EnchantmentData
import kotlin.math.sqrt


private val IconScale = 1f / sqrt(2.0f)
private val ShineScale = sqrt(2.0f)

@Composable
fun EnchantmentIconImage(
    data: EnchantmentData,
    onClick: (EnchantmentData) -> Unit = { },
    hideIndicator: Boolean = false,
    forceOpaque: Boolean = false,
    outline: Boolean = false,
    disabled: Boolean = false,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val patterns = remember(data) { data.shinePatterns?.let { EnchantmentPatterns(it[0], it[1], it[2]) } }

    Box(
        modifier = Modifier
            .then(modifier)
            .graphicsLayer { alpha = if (!forceOpaque && disabled && !selected) 0.25f else 1f }
            .scale(IconScale)
            .clickable(interaction, null, enabled = (!disabled || selected)) { onClick(data) }
            .hoverable(interaction, enabled = !disabled)
            .rotate(45f)
            .drawBehind {
                if (outline) return@drawBehind drawEnchantmentIconBorder(1f)
                if (hideIndicator) return@drawBehind
                if (!hovered && !selected) return@drawBehind
                drawEnchantmentIconBorder(if (selected) 0.8f else 0.4f)
            }
            .scale(2f),
    ) {
        BlurShadowImage(
            bitmap = data.icon,
            contentDescription = data.description,
            enabled = data.id != "Unset",
            modifier = Modifier
                .rotate(-45f)
                .scale(ShineScale)
                .scale(if (data.id == "Unset") 0.7f else 1f)
                .scale(0.5f)
        )
        if (patterns != null && data.id != "Unset") {
            EnchantmentShine(patterns)
        }
    }
}

@Composable
fun EnchantmentLevelImage(
    level: Int,
    positionerSize: Float = 0.45f,
    scale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    AnimatedContent(
        targetState = level,
        transitionSpec = {
            val a = with(density) { if (initialState < targetState) -20.dp.roundToPx() else 20.dp.roundToPx() }
            val b = with(density) { if (initialState < targetState) 20.dp.roundToPx() else -20.dp.roundToPx() }

            val enter = fadeIn() + slideIn { IntOffset(0, a) }
            val exit = fadeOut() + slideOut { IntOffset(0, b) }
            enter togetherWith exit using SizeTransform(false)
        },
        modifier = Modifier
            .fillMaxSize(positionerSize)
            .padding(10.dp)
            .then(modifier)
    ) { capturedLevel ->
        if (capturedLevel != 0) {
            Image(
                bitmap = DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${capturedLevel}_normal_text.png"],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .padding(10.dp)
            )
        } else {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .padding(10.dp)
            )
        }
    }
}

@Composable
private fun EnchantmentShine(patterns: EnchantmentPatterns) {
    val interpolation by animateShineInterpolation()
    val interpolatedR by remember { derivedStateOf { interpolateShineAlpha(interpolation, ChannelDelay * 0) } }
    val interpolatedG by remember { derivedStateOf { interpolateShineAlpha(interpolation, ChannelDelay * 1) } }
    val interpolatedB by remember { derivedStateOf { interpolateShineAlpha(interpolation, ChannelDelay * 2) } }

    val imageModifier = remember {
        Modifier
            .fillMaxSize()
            .rotate(-45f)
            .scale(ShineScale)
            .scale(0.5f)
    }

    Canvas(modifier = imageModifier) {
        drawImage(image = patterns.r, dstSize = size.round(), alpha = interpolatedR * MaxAlpha)
        drawImage(image = patterns.g, dstSize = size.round(), alpha = interpolatedG * MaxAlpha)
        drawImage(image = patterns.b, dstSize = size.round(), alpha = interpolatedB * MaxAlpha)
    }
}

private const val MaxAlpha = 0.75f

private const val ChannelDelay = 500
private const val AlphaSnap = 500
private const val AlphaDuration = 700
private const val IdleAlpha = AlphaDuration - AlphaSnap
private const val RestartDelay = 1000
private const val AnimationDuration = (AlphaDuration + ChannelDelay * 2) * 2 + RestartDelay

// [--------------------------------------------]
// [^      ^      ^         ^                   ]
// [--------------------------------------------]
// [     ^      ^      ^         ^              ]
// [--------------------------------------------]
// [          ^      ^      ^         ^         ]

@Composable
private fun animateShineInterpolation(): State<Float> =
    rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(AnimationDuration, easing = LinearEasing),
            initialStartOffset = StartOffset(1000)
        )
    )

@Stable
private fun EaseOutCubic(x: Float) = 1 - (1 - x) * (1 - x)

@Stable
private fun interpolateShineAlpha(x: Float, delay: Int): Float {
    val timestamp = x * AnimationDuration
    if (timestamp < delay || timestamp > delay + AlphaDuration * 2) return 0f

    var normalized = timestamp - delay
    return if (normalized < AlphaDuration) {
        if (normalized > AlphaSnap) 1f
        else (EaseOutCubic(normalized / AlphaSnap) * 16f).toInt() / 16f
    } else {
        normalized -= AlphaDuration
        if (normalized < IdleAlpha) 1f
        else (EaseOutCubic(1 - (normalized - (IdleAlpha)) / AlphaSnap) * 16f).toInt() / 16f
    }
}

@Immutable
data class EnchantmentPatterns(
    val r: ImageBitmap,
    val g: ImageBitmap,
    val b: ImageBitmap
) {
    inline fun forEach(block: (Int, ImageBitmap) -> Unit) {
        block(0, r)
        block(1, g)
        block(2, b)
    }
}
