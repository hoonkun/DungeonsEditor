package kiwi.hoonkun.ui.reusables

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import minecraft.dungeons.resources.DungeonsTextures

@Stable
fun VariantFilterIcon(with: String, selected: Boolean): ImageBitmap {
    val filename = when (with) {
        "Melee" -> "filter_melee"
        "Ranged" -> "filter_ranged"
        "Armor" -> "filter_armour"
        "Artifact" -> "filter_consume"
        "Enchanted" -> "filter_enchant"
        else -> throw RuntimeException("unreachable error!")
    }
    val suffix = if (selected) "" else "_default"
    return DungeonsTextures["/Game/UI/Materials/Inventory2/Filter/$filename$suffix.png"]
}

@Stable
fun RarityColor(rarity: String, type: RarityColorType): Color {
    return when (rarity) {
        "Common" ->
            when (type) {
                RarityColorType.Translucent -> Color(0x00aaaaaa)
                RarityColorType.Opaque -> Color(0xffaaaaaa)
            }
        "Rare" ->
            when (type) {
                RarityColorType.Translucent -> Color(0x3337c189)
                RarityColorType.Opaque -> Color(0xff37c189)
            }
        "Unique" ->
            when (type) {
                RarityColorType.Translucent -> Color(0x33ff7826)
                RarityColorType.Opaque -> Color(0xffff7826)
            }
        else -> throw RuntimeException("invalid rarity: $rarity")
    }
}

enum class RarityColorType {
    Translucent, Opaque
}