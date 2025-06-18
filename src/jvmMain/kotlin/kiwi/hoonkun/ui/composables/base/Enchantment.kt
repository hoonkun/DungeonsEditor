package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import kotlinx.collections.immutable.ImmutableList
import minecraft.dungeons.resources.DungeonsSkeletons
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.states.MutableDungeons


private val DefaultPaint = Paint()
private val DarknessPaint = Paint().apply {
    colorFilter = ColorFilter.lighting(Color(red = 0.65f, green = 0.65f, blue = 0.65f), Color.Black)
}

@Composable
fun EnchantmentImage(
    data: DungeonsSkeletons.Enchantment,
    modifier: Modifier = Modifier.fillMaxSize(),
    selected: Boolean = false,
    enabled: Boolean = true,
    contentPaint: (Paint) -> Paint = { it },
    onDrawFront: DrawScope.(EnchantmentDrawCache) -> Unit = { _ -> },
    onClick: ((DungeonsSkeletons.Enchantment) -> Unit)? = null,
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()

    val paint = remember(pressed, contentPaint) { contentPaint(if (pressed) DarknessPaint else DefaultPaint) }

    val patterns = data.shinePatterns

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
        if (ArcticSettings.minimizeAnimations) {
            val func: DrawScope.(EnchantmentDrawCache, IntOffset, IntSize) -> Unit = { cache, _, _ ->
                onDrawFront(cache)
            }

            func
        } else {
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
        }
    } else {
        val func: DrawScope.(EnchantmentDrawCache, IntOffset, IntSize) -> Unit = { cache, _, _ ->
            onDrawFront(cache)
        }
        func
    }

    BlurShadowImage(
        bitmap = data.icon,
        enabled = data.id != "Unset",
        drawCacheFactory = { offset, size -> EnchantmentDrawCache(EnchantmentOutlinePath(offset, size)) },
        onDrawBehind = drawBehind,
        onDrawFront = drawFront,
        contentScale = if (data.isValid()) 1f else 0.75f,
        contentPaint = { paint },
        clickableModifier = Modifier
            .clip(RhombusShape)
            .hoverable(interaction, enabled = enabled)
            .clickable(interactionSource = interaction, enabled = enabled, indication = null) { onClick?.invoke(data) },
        modifier = modifier
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

@Composable
fun EnchantmentSlot(
    enchantments: ImmutableList<MutableDungeons.Enchantment>,
    modifier: Modifier = Modifier,
    contentEach: @Composable (MutableDungeons.Enchantment) -> Unit
) {
    val offsets = remember { listOf(Offset(0f, 0.5f), Offset(0.5f, 1f), Offset(1f, 0.5f)) }

    Box(modifier = modifier) {
        val sizeModifier = Modifier.fillMaxSize(0.5f)

        Image(
            bitmap = DungeonsTextures["/UI/Materials/Inventory2/Enchantment2/enchant_icon.png"],
            contentDescription = null,
            modifier = sizeModifier.offsetRelative(0.5f, 0f).scale(0.375f)
        )

        enchantments.zip(offsets).forEach { (enchantment, offset) ->
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
    MinimizableAnimatedContent(
        targetState = level,
        transitionSpec = minimizableContentTransform spec@ {
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
        val modifier = Modifier.fillMaxSize().scale(scale)
        if (capturedLevel == 0)
            Spacer(modifier = modifier)
        else if (capturedLevel <= MutableDungeons.Enchantment.IntendedMaxLevel)
            Canvas(modifier = modifier) {
                drawImage(
                    image = DungeonsTextures["/UI/Materials/Inventory2/Enchantment/Inspector2/level_${capturedLevel}_normal_text.png"],
                    dstSize = size.round()
                )
            }
        else
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = capturedLevel.toString(),
                    color = Color(0xffe5dac8),
                    fontSize = if (scale > 1.0f) 24.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.offset(y = (-2).dp),
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

@Stable
private fun RhombusPath(size: Size) = Path().apply {
    moveTo(0.5f * size.width, 0f * size.height)
    lineTo(1f * size.width, 0.5f * size.height)
    lineTo(0.5f * size.width, 1f * size.height)
    lineTo(0.0f * size.width, 0.5f * size.height)
    close()
}

@Stable
private val RhombusShape: Shape =
    object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
            Outline.Generic(RhombusPath(size))

        override fun toString(): String = "RectangleShape"
    }
