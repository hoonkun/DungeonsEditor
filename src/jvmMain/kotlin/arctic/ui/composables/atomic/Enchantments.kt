package arctic.ui.composables.atomic

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import arctic.ui.composables.rememberMutableInteractionSource
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
            .drawBehind {
                if (!indicatorEnabled) return@drawBehind
                if (!hovered && !selected) return@drawBehind
                drawEnchantmentIconBorder(if (selected) 0.8f else 0.4f)
            }
            .scale(1f / sqrt(2.0f))
            .clickable(interaction, null) { onClick(data) }
            .hoverable(interaction)
            .rotate(45f),
        imageModifier = Modifier
            .rotate(-45f)
            .scale(sqrt(2.0f))
    )
}

@Composable
fun BoxScope.EnchantmentLevelImage(level: Int, positionerSize: Float = 0.3f, scale: Float = 1.0f) {
    Debugging.recomposition("LevelImage")

    if (level == 0) return

    LevelImagePositioner(size = positionerSize) {
        Image(
            IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${level}_normal_text.png" },
            null,
            modifier = Modifier.fillMaxSize().scale(scale)
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

