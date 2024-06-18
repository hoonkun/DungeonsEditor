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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kiwi.hoonkun.ui.reusables.drawEnchantmentIconBorder
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.resources.EnchantmentData
import kotlin.math.sqrt


@Composable
fun EnchantmentIconImage(
    data: EnchantmentData,
    onClick: (EnchantmentData) -> Unit = { },
    hideIndicator: Boolean = false,
    disableInteraction: Boolean = false,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val patterns = remember(data) { data.shinePatterns }

    Box(
        modifier = modifier
            .scale(1f / sqrt(2.0f))
            .clickable(interaction, null, enabled = (!disableInteraction || selected)) { onClick(data) }
            .hoverable(interaction, enabled = !disableInteraction)
            .rotate(45f)
            .drawBehind {
                if (hideIndicator) return@drawBehind
                if (!hovered && !selected) return@drawBehind
                drawEnchantmentIconBorder(if (selected) 0.8f else 0.4f)
            }
            .scale(2f)
            .alpha(if (disableInteraction && !selected) 0.25f else 1f),
    ) {
        BlurShadowImage(
            bitmap = data.icon,
            contentDescription = data.description,
            enabled = data.id != "Unset",
            modifier = Modifier
                .rotate(-45f)
                .scale(sqrt(2.0f))
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
private fun EnchantmentShine(patterns: List<ImageBitmap>) {
    val transition = rememberInfiniteTransition()

    val alphaR by animateShine(transition, 500 * 0)
    val alphaG by animateShine(transition, 500 * 1)
    val alphaB by animateShine(transition, 500 * 2)

    val drawShine: DrawScope.(Int, Float) -> Unit = { index, alpha ->
        drawImage(
            image = patterns[index],
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            alpha = alpha,
            blendMode = BlendMode.Overlay
        )
    }

    Canvas(modifier = Modifier.fillMaxSize().rotate(-45f).scale(sqrt(2.0f)).scale(0.5f)) {
        drawShine(0, alphaR)
        drawShine(1, alphaG)
        drawShine(2, alphaB)
    }
}

@Composable
private fun animateShine(transition: InfiniteTransition, delay: Int) =
    transition.animateFloat(
        initialValue = 0f, targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 700
                delayMillis = 1000
                0.0f at 0
                0.75f at 500
                0.75f at 700
            },
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(1000 + delay)
        )
    )
