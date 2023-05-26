package blackstone.states.items

import Database
import blackstone.states.ArmorProperty
import extensions.GameResources


private val armorPropertyRarityIconNames = mapOf(
    "Common" to "regular",
    "Unique" to "unique",
    "Rare" to "rare"
)

val ArmorProperty.data get() = Database.current.findArmorProperty(id) ?: throw RuntimeException("unknown armor property $id")

fun ArmorPropertyRarityIcon(rarity: String) =
    GameResources.image { "/Game/UI/Materials/Inventory2/Inspector/${armorPropertyRarityIconNames[rarity] ?: "regular"}_bullit.png" }
