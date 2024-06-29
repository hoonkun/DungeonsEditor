package kiwi.hoonkun.ui.composables.base

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.round
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsSkeletons
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.skeleton
import minecraft.dungeons.values.DungeonsItem


@Composable
fun ItemAlterButton(
    color: Color = Color(0x15ffffff),
    enabled: Boolean = true,
    horizontalPadding: Dp = 10.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    content: @Composable RowScope.() -> Unit
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(3.dp)
            .then(modifier)
            .requiredHeight(35.dp)
            .then(
                if (enabled)
                    Modifier
                        .clickable(interaction, null, role = Role.Button) { onClick() }
                        .hoverable(interaction)
                else
                    Modifier
            )
            .background(color, shape = RoundedCornerShape(6.dp))
            .drawBehind { drawInteractionBorder(hovered, false) }
            .padding(vertical = 4.dp, horizontal = horizontalPadding),
        content = content
    )
}

@Composable
fun ItemAlterButton(
    text: String,
    color: Color = Color(0x25ffffff),
    enabled: Boolean = true,
    onClick: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    ItemAlterButton(
        color = color,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = Color.White
        )
    }
}


@Composable
fun ItemRarityButton(
    data: DungeonsSkeletons.Item,
    rarity: DungeonsItem.Rarity,
    readonly: Boolean = false,
    onClick: (DungeonsItem.Rarity) -> Unit = { }
) {
    ItemAlterButton(
        text = "${if (data.limited) "${Localizations["season_limited"]} " else ""}${DungeonsLocalizations["/rarity_${rarity.name.lowercase()}"]}",
        color = RarityColor(rarity, RarityColorType.Translucent),
        enabled = !readonly && !data.unique,
        onClick = {
            onClick(
                if (rarity == DungeonsItem.Rarity.Common) DungeonsItem.Rarity.Rare
                else DungeonsItem.Rarity.Common)
        }
    )
}

@Composable
fun BuiltInEnchantments(data: DungeonsSkeletons.Enchantment) {
    ItemAlterButton(
        color = Color(0x25ffffff),
        enabled = false
    ) {
        Image(
            bitmap = data.icon,
            contentDescription = null,
            modifier = Modifier
                .requiredSize(30.dp)
                .drawBehind {
                    drawImage(
                        image = DungeonsTextures["/Game/Content_DLC4/UI/Materials/Inventory/gilded_bullit.png"],
                        dstSize = (size * 0.9f).round(),
                        dstOffset = (center - (size * 0.9f / 2f).let { Offset(it.width, it.height) }).round()
                    )
                }
        )
        Text(text = data.name, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun ItemNetheriteEnchantButton(
    enchantment: MutableDungeons.Enchantment?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (MutableDungeons.Enchantment) -> Unit = { }
) {
    @Composable
    fun InactiveItemNetheriteEnchantButton(
        enchantment: () -> MutableDungeons.Enchantment,
        enabled: Boolean = true
    ) {
        ItemAlterButton(
            color = Color(0x15ffffff),
            horizontalPadding = 4.dp,
            modifier = modifier,
            enabled = enabled,
            onClick = { onClick(enchantment()) }
        ) {
            Image(
                bitmap = DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png"],
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    @Composable
    fun ActiveItemNetheriteEnchantButton(
        enchantment: MutableDungeons.Enchantment,
        enabled: Boolean = true
    ) {
        ItemAlterButton(
            color = Color(0x40ffc847),
            horizontalPadding = 10.dp,
            modifier = modifier,
            enabled = enabled,
            onClick = { onClick(enchantment) }
        ) {
            MinimizableAnimatedContent(
                targetState = enchantment.skeleton,
                transitionSpec = minimizableContentTransform spec@ {
                    val enter =
                        if (targetState.id == "Unset") defaultFadeIn()
                        else defaultFadeIn() + scaleIn(initialScale = 1.5f)
                    val exit = defaultFadeOut()
                    enter togetherWith exit using SizeTransform(clip = false)
                },
                modifier = Modifier
                    .requiredSize(30.dp)
                    .drawBehind {
                        drawImage(
                            image = DungeonsTextures["/Game/Content_DLC4/UI/Materials/Inventory/gilded_bullit.png"],
                            dstSize = size.round()
                        )
                    }
                    .scale(1.05f)
            ) { capturedEnchantmentData ->
                Image(
                    bitmap = capturedEnchantmentData.icon,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = DungeonsLocalizations["AncientLabels/iteminspector_gilded"]!!,
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }

    ItemAlterButtonAnimatable(targetState = enchantment?.isUnset != false) { isUnset ->
        if (isUnset) {
            InactiveItemNetheriteEnchantButton(
                enchantment = {
                    enchantment ?: MutableDungeons.Enchantment(isNetheriteEnchant = true)
                }
            )
        } else {
            if (enchantment == null) return@ItemAlterButtonAnimatable
            ActiveItemNetheriteEnchantButton(enchantment, enabled = enabled)
        }
    }
}

@Composable
fun ItemModifiedButton(
    holder: MutableDungeons.Item,
    readonly: Boolean = false,
    hideUnits: Boolean = false,
) {
    ItemAlterButtonAnimatable(targetState = holder.modified == true) { modified ->
        ItemAlterButton(
            color = if (modified) Color(0x556f52ff) else Color(0x15ffffff),
            enabled = !readonly,
            onClick = { holder.modified = !modified }
        ) {
            Text(
                text = if (modified) Localizations["modified"] else "_",
                fontSize = 18.sp,
                color = Color.White
            )

            if (!modified) return@ItemAlterButton

            if (readonly) {
                Text(text = "${holder.timesModified}", fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
            } else {
                var timesModified by remember { mutableStateOf("${holder.timesModified ?: 0}") }
                TextFieldValidatable(
                    value = timesModified,
                    onValueChange = { timesModified = it },
                    validator = { it.toIntOrNull() != null },
                    onSubmit = { holder.timesModified = timesModified.toInt().takeIf { it != 0 } },
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.White, textAlign = TextAlign.End),
                    hideDecorationIfNotFocused = true,
                    modifier = Modifier.requiredWidth(28.dp).padding(end = 2.dp).offset(y = (-1).dp)
                )
            }
            if (!hideUnits) {
                Text(text = Localizations["times"], fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun ItemAlterButtonAnimatable(
    targetState: Boolean,
    content: @Composable (Boolean) -> Unit
) {
    MinimizableAnimatedContent(
        targetState = targetState,
        transitionSpec = minimizableContentTransform spec@ {
            val enterSpec = tween<Float>(220, delayMillis = 90)
            val enter = fadeIn(animationSpec = enterSpec) + scaleIn(initialScale = 0.92f, animationSpec = enterSpec)
            val exit = fadeOut(animationSpec = tween(90))
            enter togetherWith exit using SizeTransform(clip = false)
        },
        content = content
    )
}