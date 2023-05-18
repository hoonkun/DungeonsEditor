package states

import androidx.compose.runtime.*
import extensions.safe
import extensions.toJsonObjectArray
import io.StoredFile
import org.json.JSONObject

@Stable
class StoredFileState(from: StoredFile) {

    val items = Items(from)

}

fun StoredFile.getItems(): List<Item> {
    return root.getJSONArray("items").toJsonObjectArray { Item(it) }
}

@Stable
class Items(from: StoredFile) {

    val all = from.getItems().sortedBy { it.inventoryIndex }.toMutableStateList()

    val commons by derivedStateOf { all.filter { it.equipmentSlot == null } }

    val melee by derivedStateOf { all.find { it.equipmentSlot == Item.EquipmentSlot.MeleeGear } }
    val armor by derivedStateOf { all.find { it.equipmentSlot == Item.EquipmentSlot.ArmorGear } }
    val ranged by derivedStateOf { all.find { it.equipmentSlot == Item.EquipmentSlot.RangedGear } }

    val hotbar1 by derivedStateOf { all.find { it.equipmentSlot == Item.EquipmentSlot.HotbarSlot1 } }
    val hotbar2 by derivedStateOf { all.find { it.equipmentSlot == Item.EquipmentSlot.HotbarSlot2 } }
    val hotbar3 by derivedStateOf { all.find { it.equipmentSlot == Item.EquipmentSlot.HotbarSlot3 } }

}

@Stable
class Item(from: JSONObject) {

    val type by mutableStateOf(from.getString("type"))

    val armorProperties by mutableStateOf(
        from
            .safe { getJSONArray("armorproperties") }
            ?.toJsonObjectArray { ArmorProperty(it) }
            ?.toMutableStateList()
    )

    val enchantments by mutableStateOf(
        from
            .safe { getJSONArray("enchantments") }
            ?.toJsonObjectArray { Enchantment(it) }
            ?.toMutableStateList()
    )

    var equipmentSlot by mutableStateOf(EquipmentSlot.fromValue(from.safe { getString("equipmentSlot") }))

    var inventoryIndex by mutableStateOf(from.safe { getInt("inventoryIndex") })

    var modified by mutableStateOf(from.safe { getBoolean("modified") })

    var netheriteEnchant by mutableStateOf(from.safe { Enchantment(getJSONObject("netheriteEnchant")) })

    val power by mutableStateOf(from.getFloat("power"))

    val rarity by mutableStateOf(from.getString("rarity"))

    val timesModified by mutableStateOf(from.safe { getInt("timesmodified") })

    val upgraded by mutableStateOf(from.getBoolean("upgraded"))

    enum class EquipmentSlot {
        MeleeGear, ArmorGear, RangedGear, HotbarSlot1, HotbarSlot2, HotbarSlot3;

        companion object {
            private val valueMap = EquipmentSlot.values().associateBy { it.name }
            fun fromValue(value: String?) = if (value != null) valueMap[value] else null
        }
    }

    enum class Rarity {
        Common, Rare, Unique;

        companion object {
            private val valueMap = Rarity.values().associateBy { it.name }
            fun fromValue(value: String?) = if (value != null) valueMap[value] else null
        }
    }

}

@Stable
class Enchantment(from: JSONObject) {
    var id by mutableStateOf(from.getString("id"))
    var investedPoints by mutableStateOf(from.getInt("investedPoints"))
    var level by mutableStateOf(from.getInt("level"))
}

@Stable
class ArmorProperty(from: JSONObject) {
    var id: String by mutableStateOf(from.getString("id"))
    var rarity: String by mutableStateOf(from.getString("rarity"))
}