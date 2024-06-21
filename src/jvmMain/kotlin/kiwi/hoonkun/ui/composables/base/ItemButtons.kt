package kiwi.hoonkun.ui.composables.base

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.overlays.EnchantmentOverlay
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.Enchantment
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import kiwi.hoonkun.utils.Retriever
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.resources.ItemData

@Composable
fun ItemRarityButton(
    data: ItemData,
    rarity: String,
    readonly: Boolean = false,
    onClick: (String) -> Unit = { }
) {
    ItemAlterButton(
        text = "${if (data.limited) "${Localizations.UiText("season_limited")} " else ""}${DungeonsLocalizations["/rarity_${rarity.lowercase()}"]}",
        color = RarityColor(rarity, RarityColorType.Translucent),
        enabled = !readonly && !data.unique,
        onClick = { onClick(if (rarity == "Common") "Rare" else "Common") }
    )
}

@Composable
fun ItemAlterButton(
    text: String,
    color: Color = Color(0x25ffffff),
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(35.dp)
            .clickable(interaction, null, role = Role.Button, enabled = enabled) { onClick() }
            .hoverable(interaction, enabled)
            .background(color, shape = RoundedCornerShape(6.dp))
            .drawBehind { drawInteractionBorder(hovered, false) }
            .padding(vertical = 4.dp, horizontal = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = Color.White
        )
    }
}

@Composable
fun ItemAlterButton(
    color: Color = Color(0x15ffffff),
    enabled: Boolean = true,
    horizontalPadding: Dp = 10.dp,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(35.dp)
            .clickable(interaction, null, role = Role.Button, enabled = enabled) { onClick() }
            .hoverable(interaction, enabled)
            .background(color, shape = RoundedCornerShape(6.dp))
            .drawBehind { drawInteractionBorder(hovered, false) }
            .padding(vertical = 4.dp, horizontal = horizontalPadding),
        content = content
    )
}


@Composable
fun ItemNetheriteEnchantButton(
    holder: Item,
    readonly: Boolean = false,
) {
    val overlays = LocalOverlayState.current

    @Composable
    fun InactiveItemNetheriteEnchantButton(builder: Retriever<Enchantment>) {
        ItemAlterButton(
            color = Color(0x15ffffff),
            horizontalPadding = 4.dp,
            enabled = !readonly,
            onClick = {
                val target = builder()
                overlays.make(enter = defaultFadeIn(), exit = defaultFadeOut()) {
                    EnchantmentOverlay(
                        initialSelected = target,
                        requestClose = { overlays.destroy(it) }
                    )
                }
            }
        ) {
            Image(
                bitmap = DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png"],
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    @Composable
    fun ActiveItemNetheriteEnchantButton(enchantment: Enchantment) {
        ItemAlterButton(
            color = Color(0x40ffc847),
            horizontalPadding = 10.dp,
            enabled = !readonly,
            onClick = {
                overlays.make(enter = defaultFadeIn(), exit = defaultFadeOut()) {
                    EnchantmentOverlay(
                        initialSelected = enchantment,
                        requestClose = { overlays.destroy(it) }
                    )
                }
            }
        ) {
            Image(
                bitmap = enchantment.data.icon,
                contentDescription = null,
                modifier = Modifier
                    .requiredSize(30.dp)
                    .drawBehind {
                        drawImage(
                            image = DungeonsTextures["/Game/Content_DLC4/UI/Materials/Inventory/gilded_bullit.png"],
                            dstSize = IntSize(size.width.toInt(), size.height.toInt())
                        )
                    }
                    .scale(1.05f)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = DungeonsLocalizations["AncientLabels/iteminspector_gilded"]!!, fontSize = 18.sp, color = Color.White)
        }
    }

    val enchantment = holder.netheriteEnchant

    if (enchantment != null && !enchantment.isUnset)
        ActiveItemNetheriteEnchantButton(enchantment)
    else
        InactiveItemNetheriteEnchantButton(builder = { holder.newNetheriteEnchant() })
}

@Composable
fun ItemModifiedButton(
    holder: Item,
    readonly: Boolean = false
) {
    val modified = holder.modified == true

    ItemAlterButton(
        color = if (modified) Color(0x556f52ff) else Color(0x15ffffff),
        enabled = !readonly,
        onClick = { holder.modified = !modified }
    ) {
        Text(
            text = if (modified) Localizations.UiText("modified") else "_",
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
        Text(text = Localizations.UiText("times"), fontSize = 18.sp)
    }
}
