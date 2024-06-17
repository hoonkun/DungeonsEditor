package kiwi.hoonkun.ui.reusables

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

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