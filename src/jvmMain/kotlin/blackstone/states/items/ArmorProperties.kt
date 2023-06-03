package blackstone.states.items

import dungeons.Database
import blackstone.states.ArmorProperty
import blackstone.states.Item
import extensions.GameResources


private val armorPropertyRarityIconNames = mapOf(
    "Common" to "regular",
    "Unique" to "unique",
    "Rare" to "rare"
)

val ArmorProperty.data get() = Database.armorProperty(id) ?: throw RuntimeException("unknown armor property $id")

fun ArmorProperty(id: String, holder: Item, rarity: String = "Common") =
    ArmorProperty(id, rarity).apply { this.holder = holder }

fun ArmorProperty(other: ArmorProperty) =
    ArmorProperty(other).apply { this.holder = other.holder }

fun ArmorPropertyRarityIcon(rarity: String) =
    GameResources.image { "/Game/UI/Materials/Inventory2/Inspector/${armorPropertyRarityIconNames[rarity] ?: "regular"}_bullit.png" }
