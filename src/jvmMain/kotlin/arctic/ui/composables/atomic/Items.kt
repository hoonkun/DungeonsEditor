package arctic.ui.composables.atomic

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import dungeons.IngameImages

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
    return IngameImages.get { "/Game/UI/Materials/Inventory2/Filter/$filename$suffix.png" }
}

enum class RarityColorType {
    Translucent, Opaque
}

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