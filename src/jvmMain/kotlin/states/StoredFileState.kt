package states

import Database
import EnchantmentData
import Localizations
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import extensions.*
import io.StoredFile
import org.json.JSONObject
import java.io.File

@Stable
class StoredFileState(from: StoredFile) {

    val items = Items(from)

    val currency = Currencies(from)

    var level by mutableStateOf(from.root.getLong("xp"))

    val power: Int get() {
        val powerDividedBy4 = listOf(
            items.melee?.power ?: 0f,
            items.armor?.power ?: 0f,
            items.ranged?.power ?: 0f
        ).map { DungeonsPower.toInGamePower(it) }.sum() / 4f
        val powerDividedBy12 = listOf(
            items.hotbar1?.power ?: 0f,
            items.hotbar2?.power ?: 0f,
            items.hotbar3?.power ?: 0f
        ).map { DungeonsPower.toInGamePower(it) }.sum() / 12f
        return (powerDividedBy4 + powerDividedBy12).toInt()
    }

}

@Stable
class Currencies(from: StoredFile) {
    private val items = from.root.getJSONArray("currency").toJsonObjectArray { Currency(it) }
    var emerald by mutableStateOf(items.find { it.type == "Emerald" }?.count ?: 0)
    var gold by mutableStateOf(items.find { it.type == "Gold" }?.count ?: 0)
    var eyeOfEnder by mutableStateOf(items.find { it.type == "EyeOfEnder" }?.count ?: 0)
}

@Stable
class Currency(from: JSONObject) {
    val type: String = from.getString("type")
    val count: Int = from.getInt("count")
}

@Stable
class Items(from: StoredFile) {

    val all = from.root
        .getJSONArray("items")
        .toJsonObjectArray { Item(it) }
        .sortedBy { it.inventoryIndex }
        .toMutableStateList()

    val melee by derivedStateOf { all.find { it.equipmentSlot == Item.Slot.MeleeGear } }
    val armor by derivedStateOf { all.find { it.equipmentSlot == Item.Slot.ArmorGear } }
    val ranged by derivedStateOf { all.find { it.equipmentSlot == Item.Slot.RangedGear } }

    val hotbar1 by derivedStateOf { all.find { it.equipmentSlot == Item.Slot.HotbarSlot1 } }
    val hotbar2 by derivedStateOf { all.find { it.equipmentSlot == Item.Slot.HotbarSlot2 } }
    val hotbar3 by derivedStateOf { all.find { it.equipmentSlot == Item.Slot.HotbarSlot3 } }

    val equipped by derivedStateOf { listOf(melee, armor, ranged, hotbar1, hotbar2, hotbar3) }

    val unequipped by derivedStateOf { all.filter { it.equipmentSlot == Item.Slot.InventoryIndex } }

    companion object {
        val filter: (item: Item, Item.ItemType?, Item.Rarity?) -> Boolean = { it, type, rarity ->
            (type == null || it.Type() == type) && (rarity == null || it.rarity == rarity)
        }
    }

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
            ?.toJsonObjectArray { Enchantment(this@Item, it) }
            ?.toMutableStateList()
    )

    val enchantmentSlots by derivedStateOf { enchantments?.chunked(3)?.map { EnchantmentSlot(it) } }

    var equipmentSlot by mutableStateOf(from.safe { Slot.fromValue(getString("equipmentSlot")) } ?: Slot.InventoryIndex)

    var inventoryIndex by mutableStateOf(from.safe { getInt("inventoryIndex") })

    var modified by mutableStateOf(from.safe { getBoolean("modified") })

    var netheriteEnchant by mutableStateOf(from.safe { Enchantment(this@Item, getJSONObject("netheriteEnchant")) })

    var power by mutableStateOf(from.getFloat("power"))

    val rarity by mutableStateOf(Rarity.fromValue(from.getString("rarity")))

    val timesModified by mutableStateOf(from.safe { getInt("timesmodified") })

    val upgraded by mutableStateOf(from.getBoolean("upgraded"))

    fun Name(): String = Localizations.ItemName(type)

    fun Flavour(): String? = Localizations.ItemFlavour(type)
    fun Description(): String? = Localizations.ItemDescription(type)

    fun TotalInvestedEnchantmentPoints() = enchantments?.sumOf { it.investedPoints } ?: 0

    fun InventoryIcon(): ImageBitmap {
        val cached = GameResources.image("$type-Inventory")
        if (cached != null) return cached

        val imagePath = Database.current.findGear(type)?.dataPath ?: Database.current.findArtifact(type)?.dataPath ?: throw RuntimeException("unknown item type!")
        val dataDirectory = File("${Constants.GameDataDirectoryPath}${imagePath}")
        val imageFile = dataDirectory.listFiles().let { files ->
            files?.find { it.extension == "png" && it.name.lowercase().endsWith("_icon_inventory.png") }
        } ?: throw RuntimeException("no image resource found!")

        return GameResources.image(type, false) { imageFile.absolutePath }
    }

    fun LargeIcon(): ImageBitmap {
        val cached = GameResources.image("$type-Large")
        if (cached != null) return cached

        val imagePath = Database.current.findGear(type)?.dataPath ?: Database.current.findArtifact(type)?.dataPath ?: throw RuntimeException("unknown item type!")
        val dataDirectory = File("${Constants.GameDataDirectoryPath}${imagePath}")
        val imageFile = dataDirectory.listFiles().let { files ->
            files?.find { it.extension == "png" && it.name.lowercase().endsWith("_icon.png") }
                ?: files?.find { it.extension == "png" && it.name.lowercase().endsWith("_icon_inventory.png") }
        } ?: throw RuntimeException("no image resource found!")

        return GameResources.image(type, false) { imageFile.absolutePath }
    }

    fun Type(): ItemType {
        val gear = Database.current.findGear(type)
        val artifact = Database.current.findArtifact(type)
        return if (gear != null) {
            when (gear.type) {
                "M" -> ItemType.Melee
                "A" -> ItemType.Armor
                "R" -> ItemType.Ranged
                "U" -> ItemType.Unknown
                else -> throw RuntimeException("unreachable exception!")
            }
        } else if (artifact != null) {
            ItemType.Artifact
        } else {
            ItemType.Unknown
        }
    }

    enum class ItemType {
        Melee, Armor, Ranged, Artifact, Unknown;

        fun FilterIconName(selected: Boolean): String {
            val filename = when (this) {
                Melee -> "filter_melee"
                Ranged -> "filter_ranged"
                Armor -> "filter_armour"
                Artifact -> "filter_consume"
                else -> throw RuntimeException("unreachable error!")
            }
            val suffix = if (selected) "" else "_default"
            return "$filename$suffix"
        }

        fun DropIconName(): String {
            return when (this) {
                Melee -> "drops_melee.png"
                Ranged -> "drops_ranged.png"
                Artifact -> "drops_item.png"
                Armor -> "drops_armour.png"
                else -> "drop_unknown.png"
            }
        }
    }

    enum class Slot {
        MeleeGear, ArmorGear, RangedGear, HotbarSlot1, HotbarSlot2, HotbarSlot3, InventoryIndex;

        companion object {
            private val valueMap = Slot.values().associateBy { it.name }
            fun fromValue(value: String) = valueMap.getValue(value)
        }
    }

    enum class Rarity {
        Common, Rare, Unique;

        fun TranslucentColor(): Color {
            return when (this) {
                Common -> Color.Transparent
                Rare -> Color(0x3337c189)
                Unique -> Color(0x33ff7826)
            }
        }
        fun OpaqueColor(): Color {
            return when (this) {
                Common -> Color(0xffaaaaaa)
                Rare -> Color(0xff37c189)
                Unique -> Color(0xffff7826)
            }
        }

        fun FrameName(): String {
            return when (this) {
                Item.Rarity.Common -> "regular_frame.png"
                Item.Rarity.Rare -> "rare_frame.png"
                Item.Rarity.Unique -> "Unique_frame.png"
            }
        }

        companion object {
            private val valueMap = Rarity.values().associateBy { it.name }
            fun fromValue(value: String) = valueMap.getValue(value)
        }
    }

}

@Stable
class Enchantment(val holder: Item, from: JSONObject) {
    var id by mutableStateOf(from.getString("id"))
    var investedPoints by mutableStateOf(from.getInt("investedPoints"))
    var level by mutableStateOf(from.getInt("level"))

    val data by derivedStateOf { Database.current.enchantments.find { it.id == id } ?: throw RuntimeException("Unrecognizable enchantment received: $id") }

    fun Image() = data.Image()

    fun ImageScale() = data.ImageScale()

    fun ShineImage() = data.ShineImage()

    fun adjustLevel(level: Int) {
        this.level = level
        this.investedPoints =
            if (!data.powerful && holder.netheriteEnchant == null)
                EnchantmentData.CommonNonGlidedInvestedPoints.slice(0 until level).sum()
            else if (data.powerful && holder.netheriteEnchant == null)
                EnchantmentData.PowerfulNonGlidedInvestedPoints.slice(0 until level).sum()
            else if (!data.powerful && holder.netheriteEnchant != null)
                EnchantmentData.CommonGlidedInvestedPoints.slice(0 until level).sum()
            else if (data.powerful && holder.netheriteEnchant != null)
                EnchantmentData.PowerfulGlidedInvestedPoints.slice(0 until level).sum()
            else 0
    }

}

@Stable
class EnchantmentSlot(
    enchantments: List<Enchantment>
) {
    val enchantments = enchantments.toMutableStateList()

    private val first by derivedStateOf { enchantments[0] }
    private val second by derivedStateOf { enchantments[1] }
    private val third by derivedStateOf { enchantments[2] }

    val activatedEnchantment by derivedStateOf {
        if (first.level > 0) first
        else if (second.level > 0) second
        else if (third.level > 0) third
        else null
    }
}

@Stable
class ArmorProperty(from: JSONObject) {
    var id: String by mutableStateOf(from.getString("id"))
    var rarity: String by mutableStateOf(from.getString("rarity"))

    fun Description() =
        Localizations["ArmorProperties/${id}_description"]
            ?.replace("{0}", "")
            ?.replace("{1}", "")
            ?.replace("{2}", "")
            ?.replace("개의", "추가")
            ?.replace("  ", " ")
            ?.trim()

    fun IconName() =
        when (rarity.lowercase()) {
            "common" -> "regular"
            "unique" -> "unique"
            "rare" -> "rare" // Is this even exists?
            else -> "regular"
        }

}