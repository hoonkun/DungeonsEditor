package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kiwi.hoonkun.ui.reusables.offsetRelative
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.resources.EnchantmentData
import kotlin.math.sqrt


@Composable
fun EnchantmentImage(
    data: EnchantmentData,
    modifier: Modifier = Modifier.fillMaxSize(),
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: ((EnchantmentData) -> Unit)? = null
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()

    val patterns = remember(data) { data.shinePatterns?.let { EnchantmentPatterns(it[0], it[1], it[2]) } }

    val enchantmentPath: CacheDrawScope.(IntOffset, IntSize) -> Path = { offset, size ->
        Path().apply {
            val x = offset.x.toFloat()
            val y = offset.y.toFloat()
            val width = size.width.toFloat()
            val height = size.height.toFloat()
            moveTo(x + width / 2f, y)
            lineTo(x + width, y + height / 2f)
            lineTo(x + width / 2f, y + height)
            lineTo(x, y + height / 2f)
            close()
        }
    }

    val drawPressIndication: DrawScope.(EnchantmentDrawCache) -> Unit = indication@ { cache ->
        if (!pressed) return@indication
        scale(0.8f) { drawPath(path = cache.path, color = Color.Black.copy(alpha = 0.25f)) }
    }

    val onDrawBehind: DrawScope.(EnchantmentDrawCache, IntOffset, IntSize) -> Unit = { cache, _, _ ->
        scale(0.825f) {
            drawPath(
                path = cache.path,
                color = Color.White.copy(alpha = if (selected) 1f else if (hovered) 0.4f else 0f),
                style = Stroke(width = (6 * (1f / 0.825f)).dp.toPx())
            )
        }
    }

    val onDrawFront = if (patterns != null) {
        val interpolation by animateShineInterpolation()
        val interpolatedR by remember { derivedStateOf { interpolateShineAlpha(interpolation, ChannelDelay * 0) } }
        val interpolatedG by remember { derivedStateOf { interpolateShineAlpha(interpolation, ChannelDelay * 1) } }
        val interpolatedB by remember { derivedStateOf { interpolateShineAlpha(interpolation, ChannelDelay * 2) } }

        val func: DrawScope.(EnchantmentDrawCache, IntOffset, IntSize) -> Unit = { cache, offset, size ->
            drawImage(image = patterns.r, dstOffset = offset, dstSize = size, alpha = interpolatedR * MaxAlpha)
            drawImage(image = patterns.g, dstOffset = offset, dstSize = size, alpha = interpolatedG * MaxAlpha)
            drawImage(image = patterns.b, dstOffset = offset, dstSize = size, alpha = interpolatedB * MaxAlpha)

            drawPressIndication(cache)
        }
        func
    } else {
        val func: DrawScope.(EnchantmentDrawCache, IntOffset, IntSize) -> Unit = { cache, _, _ ->
            drawPressIndication(cache)
        }
        func
    }

    BlurShadowImage(
        bitmap = data.icon,
        enabled = data.id != "Unset",
        drawCacheFactory = { offset, size -> EnchantmentDrawCache(enchantmentPath(offset, size)) },
        onDrawBehind = onDrawBehind,
        onDrawFront = onDrawFront,
        contentScale = if (data.id == "Unset") 0.75f else 1f,
        modifier = Modifier
            .rotate(degrees = 45f)
            .scale(1f / sqrt(2f))
            .hoverable(interaction, enabled = enabled)
            .clickable(interactionSource = interaction, enabled = enabled, indication = null) { onClick?.invoke(data) }
            .scale(sqrt(2f))
            .rotate(degrees = -45f)
            .then(modifier)
    )
}

@Stable
data class EnchantmentDrawCache(val path: Path): BlurShadowImageDrawCache

@Composable
fun EnchantmentSlot(
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
    third: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val offsets = remember { listOf(Offset(0f, 0.5f), Offset(0.5f, 1f), Offset(1f, 0.5f)) }

    Box(modifier = modifier) {
        val sizeModifier = Modifier.fillMaxSize(0.5f)

        Image(
            bitmap = DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment2/enchant_icon.png"],
            contentDescription = null,
            modifier = sizeModifier.offsetRelative(0.5f, 0f).scale(0.375f)
        )

        Box(modifier = sizeModifier.offsetRelative(offsets[0])) { first() }
        Box(modifier = sizeModifier.offsetRelative(offsets[1])) { second() }
        Box(modifier = sizeModifier.offsetRelative(offsets[2])) { third() }
    }
}

@Composable
fun EnchantmentLevel(
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
private data class EnchantmentPatterns(
    val r: ImageBitmap,
    val g: ImageBitmap,
    val b: ImageBitmap
)