package kiwi.hoonkun.ui.reusables

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.values.DungeonsItem

@Stable
fun VariantFilterIcon(with: DungeonsItem.IconFilterable, selected: Boolean): ImageBitmap {
    val filename = when (with) {
        DungeonsItem.Variant.Melee -> "filter_melee"
        DungeonsItem.Variant.Ranged -> "filter_ranged"
        DungeonsItem.Variant.Armor -> "filter_armour"
        DungeonsItem.Variant.Artifact -> "filter_consume"
        DungeonsItem.Attributes.Enchanted -> "filter_enchant"
    }
    val suffix = if (selected) "" else "_default"
    return DungeonsTextures["/UI/Materials/Inventory2/Filter/$filename$suffix.png"]
}

@Stable
fun RarityColor(rarity: DungeonsItem.Rarity, type: RarityColorType): Color {
    return when (rarity) {
        DungeonsItem.Rarity.Common ->
            when (type) {
                RarityColorType.Translucent -> Color(0x25ffffff)
                RarityColorType.Opaque -> Color(0xffaaaaaa)
            }
        DungeonsItem.Rarity.Rare ->
            when (type) {
                RarityColorType.Translucent -> Color(0x3337c189)
                RarityColorType.Opaque -> Color(0xff37c189)
            }
        DungeonsItem.Rarity.Unique ->
            when (type) {
                RarityColorType.Translucent -> Color(0x33ff7826)
                RarityColorType.Opaque -> Color(0xffff7826)
            }
    }
}

enum class RarityColorType {
    Translucent, Opaque
}