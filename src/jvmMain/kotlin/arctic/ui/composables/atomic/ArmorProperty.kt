package arctic.ui.composables.atomic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import dungeons.IngameImages
import dungeons.states.ArmorProperty


private val nameMap = mapOf(
    "Common" to "regular",
    "Unique" to "unique",
    "Rare" to "rare"
)

@Composable
fun rememberArmorPropertyIconAsState(property: ArmorProperty): State<ImageBitmap> {
    return remember(property) {
        derivedStateOf {
            IngameImages.get { "/Game/UI/Materials/Inventory2/Inspector/${nameMap.getValue(property.rarity)}_bullit.png" }
        }
    }
}