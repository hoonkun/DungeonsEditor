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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kiwi.hoonkun.ui.reusables.offsetRelative
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.reusables.round
import kiwi.hoonkun.ui.states.Enchantment
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.resources.EnchantmentData
import kotlin.math.sqrt


private val DefaultPaint = Paint()
private val DarknessPaint = Paint().apply {
    colorFilter = ColorFilter.lighting(Color(red = 0.65f, green = 0.65f, blue = 0.65f), Color.Black)
}

@Composable
fun EnchantmentImage(
    data: EnchantmentData,
    modifier: Modifier = Modifier.fillMaxSize(),
    selected: Boolean = false,
    enabled: Boolean = true,
    contentPaint: (Paint) -> Paint = { it },
    onDrawFront: DrawScope.(EnchantmentDrawCache) -> Unit = { _ -> },
    onClick: ((EnchantmentData) -> Unit)? = null,
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()

    val paint = remember(pressed, contentPaint) { contentPaint(if (pressed) DarknessPaint else DefaultPaint) }

    val patterns = remember(data) { data.shinePatterns?.let { EnchantmentPatterns(it[0], it[1], it[2]) } }

    val drawBehind: DrawScope.(EnchantmentDrawCache, IntOffset, IntSize) -> Unit = { cache, _, _ ->
        scale(0.825f) {
            drawPath(
                path = cache.path,
                color = Color.White.copy(alpha = if (selected) 1f else if (hovered) 0.4f else 0f),
                style = Stroke(width = (6 * (1f / 0.825f)).dp.toPx())
            )
        }
    }

    val drawFront = if (patterns != null) {
        val interpolation by animateShineInterpolation()
        val interpolatedR by remember { derivedStateOf { interpolateShineAlpha(interpolation, ChannelDelay * 0) } }
        val interpolatedG by remember { derivedStateOf { interpolateShineAlpha(interpolation, ChannelDelay * 1) } }
        val interpolatedB by remember { derivedStateOf { interpolateShineAlpha(interpolation, ChannelDelay * 2) } }

        val func: DrawScope.(EnchantmentDrawCache, IntOffset, IntSize) -> Unit = { cache, offset, size ->
            drawIntoCanvas {
                it.saveLayer(Rect(0f, 0f, this.size.width, this.size.height), paint)
                drawImage(image = patterns.r, dstOffset = offset, dstSize = size, alpha = interpolatedR * MaxAlpha)
                drawImage(image = patterns.g, dstOffset = offset, dstSize = size, alpha = interpolatedG * MaxAlpha)
                drawImage(image = patterns.b, dstOffset = offset, dstSize = size, alpha = interpolatedB * MaxAlpha)
                it.restore()
            }

            onDrawFront(cache)
        }
        func
    } else {
        val func: DrawScope.(EnchantmentDrawCache, IntOffset, IntSize) -> Unit = { cache, _, _ ->
        }
        func
    }

    BlurShadowImage(
        bitmap = data.icon,
        enabled = data.id != "Unset",
        drawCacheFactory = { offset, size -> EnchantmentDrawCache(EnchantmentOutlinePath(offset, size)) },
        onDrawBehind = drawBehind,
        onDrawFront = drawFront,
        contentScale = if (data.id == "Unset") 0.75f else 1f,
        contentPaint = { paint },
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
fun EnchantmentOutlinePath(offset: IntOffset, size: IntSize): Path =
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

@Stable
data class EnchantmentDrawCache(val path: Path): BlurShadowImageDrawCache

@Immutable
data class EnchantmentsHolder(
    val all: List<Enchantment>
)

@Composable
fun EnchantmentSlot(
    enchantments: EnchantmentsHolder,
    modifier: Modifier = Modifier,
    contentEach: @Composable (Enchantment) -> Unit
) {
    val offsets = remember { listOf(Offset(0f, 0.5f), Offset(0.5f, 1f), Offset(1f, 0.5f)) }

    Box(modifier = modifier) {
        val sizeModifier = Modifier.fillMaxSize(0.5f)

        Image(
            bitmap = DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment2/enchant_icon.png"],
            contentDescription = null,
            modifier = sizeModifier.offsetRelative(0.5f, 0f).scale(0.375f)
        )

        enchantments.all.zip(offsets).forEach { (enchantment, offset) ->
            Box(sizeModifier.offsetRelative(offset)) { contentEach(enchantment) }
        }
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
            val offset = with(density) { (if (initialState < targetState) (-20).dp else 20.dp).roundToPx() }
            val enter = fadeIn() + slideIn { IntOffset(0, offset) }
            val exit = fadeOut() + slideOut { IntOffset(0, -offset) }

            enter togetherWith exit using SizeTransform(false)
        },
        modifier = Modifier
            .fillMaxSize(positionerSize)
            .padding(10.dp)
            .then(modifier)
    ) { capturedLevel ->
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .drawBehind {
                    if (capturedLevel == 0) return@drawBehind
                    drawImage(
                        image = DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${capturedLevel}_normal_text.png"],
                        dstSize = size.round()
                    )
                }
        )
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