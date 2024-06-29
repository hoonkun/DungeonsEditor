package kiwi.hoonkun.ui.composables.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.values.DungeonsArmorProperty

private val nameMap = mapOf(
    DungeonsArmorProperty.Rarity.Common to "regular",
    DungeonsArmorProperty.Rarity.Unique to "unique",
)

@Composable
fun rememberArmorPropertyIcon(property: MutableDungeons.ArmorProperty): ImageBitmap =
    remember(property.rarity) {
        DungeonsTextures["/Game/UI/Materials/Inventory2/Inspector/${nameMap.getValue(property.rarity)}_bullit.png"]
    }