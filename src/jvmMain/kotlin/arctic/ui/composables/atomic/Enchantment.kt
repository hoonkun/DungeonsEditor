package arctic.ui.composables.atomic

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import arctic.ui.utils.rememberMutableInteractionSource
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.EnchantmentData
import dungeons.IngameImages
import dungeons.Localizations
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

    BlurEffectedImage(
        bitmap = data.icon,
        enabled = data.id != "Unset",
        containerModifier = modifier
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
        imageModifier = Modifier
            .rotate(-45f)
            .scale(sqrt(2.0f))
            .scale(if (data.id == "Unset") 0.7f else 1f)
            .scale(0.5f)
    ) {
        if (patterns != null && data.id != "Unset") {
            EnchantmentShine(patterns)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BoxScope.EnchantmentLevelImage(level: Int, positionerSize: Float = 0.45f, scale: Float = 1.0f) {
    LevelImagePositioner(size = positionerSize) {
        AnimatedContent(
            targetState = level,
            transitionSpec = {
                val enter = fadeIn() +
                    if (initialState < targetState) slideIn { IntOffset(0, -20.dp.value.toInt()) }
                    else slideIn { IntOffset(0, 20.dp.value.toInt()) }
                val exit = fadeOut() +
                    if (initialState < targetState) slideOut { IntOffset(0, 20.dp.value.toInt()) }
                    else slideOut { IntOffset(0, -20.dp.value.toInt()) }
                enter with exit using SizeTransform(false)
            },
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) { level ->
            if (level != 0) {
                Image(
                    bitmap = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${level}_normal_text.png" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().scale(scale).padding(10.dp)
                )
            } else {
                Spacer(modifier = Modifier.fillMaxSize().scale(scale).padding(10.dp))
            }
        }
    }
}

@Composable
private fun BoxScope.LevelImagePositioner(size: Float = 0.3f, content: @Composable BoxScope.() -> Unit) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(size).align(Alignment.TopEnd),
        content = content
    )

@Composable
fun PowerfulEnchantmentIndicator() =
    Text(
        text = Localizations["/enchantment_rarity_powerful"]!!,
        style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color(0xffe5247e)),
        modifier = Modifier.padding(start = 10.dp, bottom = 3.dp)
    )

@Stable
private fun ShinePatternMatrix(channel: Int): FloatArray {
    val result = FloatArray(20) { 0f }
    result[channel] = 1f
    result[channel + 5] = 1f
    result[channel + 10] = 1f

    return result
}

@Composable
fun animateShine(transition: InfiniteTransition, delay: Int) =
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
