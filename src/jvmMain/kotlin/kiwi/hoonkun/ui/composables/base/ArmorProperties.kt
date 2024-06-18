package kiwi.hoonkun.ui.composables.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import kiwi.hoonkun.ui.states.ArmorProperty
import minecraft.dungeons.resources.DungeonsTextures

private val nameMap = mapOf(
    "Common" to "regular",
    "Unique" to "unique",
    "Rare" to "rare"
)

@Composable
fun rememberArmorPropertyIconAsState(property: ArmorProperty): ImageBitmap =
    remember(property.rarity) {
        DungeonsTextures["/Game/UI/Materials/Inventory2/Inspector/${nameMap.getValue(property.rarity)}_bullit.png"]
    }