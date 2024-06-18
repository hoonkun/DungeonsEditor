package kiwi.hoonkun.ui.composables.base

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.reusables.RarityColor
import kiwi.hoonkun.ui.reusables.RarityColorType
import kiwi.hoonkun.ui.reusables.drawInteractionBorder
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.ItemData

@Composable
fun ItemRarityButton(
    data: ItemData,
    rarity: String,
    onClick: (String) -> Unit
) {
    ItemAlterButton(
        text = "${if (data.limited) "${Localizations.UiText("season_limited")} " else ""}${DungeonsLocalizations["/rarity_${rarity.lowercase()}"]}",
        color = RarityColor(rarity, RarityColorType.Translucent),
        enabled = !data.unique,
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
