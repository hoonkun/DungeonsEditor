package arctic.ui.composables.atomic

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    indicatorEnabled: Boolean = true,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (EnchantmentData) -> Unit = { }
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    BlurEffectedImage(
        bitmap = data.icon,
        enabled = data.id != "Unset",
        containerModifier = modifier
            .scale(1f / sqrt(2.0f))
            .clickable(interaction, null) { onClick(data) }
            .hoverable(interaction)
            .rotate(45f)
            .drawBehind {
                if (!indicatorEnabled) return@drawBehind
                if (!hovered && !selected) return@drawBehind
                drawEnchantmentIconBorder(if (selected) 0.8f else 0.4f)
            }
            .scale(2f),
        imageModifier = Modifier
            .rotate(-45f)
            .scale(sqrt(2.0f))
            .scale(if (data.id == "Unset") 0.7f else 1f)
            .scale(0.5f)
    ) {
        val pattern = data.shinePattern
        if (pattern != null) {
            EnchantmentShine(pattern, 0) { ShinePatternMatrix(0, it) }
            EnchantmentShine(pattern, 500) { ShinePatternMatrix(1, it) }
            EnchantmentShine(pattern, 1000) { ShinePatternMatrix(2, it) }
        }
    }
}

@Composable
fun BoxScope.EnchantmentLevelImage(level: Int, positionerSize: Float = 0.45f, scale: Float = 1.0f) {
    if (level == 0) return

    LevelImagePositioner(size = positionerSize) {
        Image(
            IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${level}_normal_text.png" },
            null,
            modifier = Modifier.fillMaxSize().scale(scale).padding(10.dp)
        )
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


private fun ShinePatternMatrix(channel: Int, alpha: Float): FloatArray {
    val result = MutableList(20) { 0f }
    result[channel] = 1f
    result[channel + 5] = 1f
    result[channel + 10] = 1f
    result[channel + 15] = alpha

    return result.toFloatArray()
}

@Composable
private fun EnchantmentShine(pattern: ImageBitmap, delay: Int, matrix: (Float) -> FloatArray) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 700
                delayMillis = 500
                0.0f at 0
                1.0f at 700
                1.0f at 1000
            },
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delay)
        )
    )

    Canvas(modifier = Modifier.fillMaxSize().rotate(-45f).scale(sqrt(2.0f)).scale(0.5f)) {
        drawImage(
            image = pattern,
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            colorFilter = ColorFilter.colorMatrix(ColorMatrix(matrix(alpha))),
            blendMode = BlendMode.Overlay
        )
    }
}
