package arctic.ui.composables.atomic

import androidx.compose.ui.graphics.ImageBitmap
import dungeons.IngameImages

fun ArmorPropertyRarityIcon(rarity: String): ImageBitmap {
    val name =
        when (rarity) {
            "Common" -> "regular"
            "Unique" -> "unique"
            "Rare" -> "rare"
            else -> "regular"
        }
    return IngameImages.get { "/Game/UI/Materials/Inventory2/Inspector/${name}_bullit.png" }
}