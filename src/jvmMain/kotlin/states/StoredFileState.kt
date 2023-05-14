package states

import androidx.compose.runtime.*
import extensions.safe
import extensions.toJsonObjectArray
import io.StoredFile
import org.json.JSONObject

@Stable
class StoredFileState(from: StoredFile) {

    val items = mutableStateListOf<Item>().apply { addAll(from.getItems()) }

}

fun StoredFile.getItems(): List<Item> {
    return root.getJSONArray("items")
        .toJsonObjectArray()
        .map { Item(it) }
}

@Stable
class Item(from: JSONObject) {

    val type by mutableStateOf(from.getString("type"))

    val armorProperties by mutableStateOf(
        from
            .safe { getJSONArray("armorproperties") }
            ?.toJsonObjectArray()
            ?.map { ArmorProperty(it) }
            ?.let { mutableStateListOf<ArmorProperty>().apply { addAll(it) } }
    )

    val enchantments by mutableStateOf(
        from
            .safe { getJSONArray("enchantments") }
            ?.toJsonObjectArray()
            ?.map { Enchantment(it) }
            ?.let { mutableStateListOf<Enchantment>().apply { addAll(it) } }
    )

    var equipmentSlot by mutableStateOf(from.safe { getString("equipmentSlot") })

    var inventoryIndex by mutableStateOf(from.safe { getInt("inventoryIndex") })

    var modified by mutableStateOf(from.safe { getBoolean("modified") })

    var netheriteEnchant by mutableStateOf(from.safe { getJSONObject("netheriteEnchant") }?.let { Enchantment(it) })

    val power by mutableStateOf(from.getFloat("power"))

    val rarity by mutableStateOf(from.getString("rarity"))

    val timesModified by mutableStateOf(from.safe { getInt("timesmodified") })

    val upgraded by mutableStateOf(from.getBoolean("upgraded"))

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