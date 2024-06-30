package minecraft.dungeons.states

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import kiwi.hoonkun.utils.*
import minecraft.dungeons.values.DungeonsArmorProperty
import minecraft.dungeons.values.DungeonsItem
import minecraft.dungeons.values.DungeonsLevel
import minecraft.dungeons.values.DungeonsPower
import org.json.JSONObject
import java.util.*
import kotlin.math.roundToInt

@Stable
class MutableDungeons(
    private val from: JSONObject
) {
    val currencies: SnapshotStateList<Currency> =
        from.getJSONArray(FIELD_CURRENCIES)
            .transformWithJsonObject { Currency(it) }
            .toMutableStateList()

    var name: String by mutableStateOf(from.getString(FIELD_NAME))

    val allItems: SnapshotStateList<Item> =
        from.getJSONArray(FIELD_ITEMS)
            .transformWithJsonObject { Item(it) }
            .toMutableStateList()

    val inventoryItems by derivedStateOf { allItems.filter { it.inventoryIndex != null } }
    val storageItems: SnapshotStateList<Item> =
        from.getJSONArray(FIELD_STORAGE_CHEST_ITEMS)
            .transformWithJsonObject { Item(it) }
            .toMutableStateList()

    val equippedItems by derivedStateOf {
        listOf(
            allItems.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.Melee },
            allItems.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.Armor },
            allItems.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.Ranged },
            allItems.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.HotBar1 },
            allItems.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.HotBar2 },
            allItems.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.HotBar3 }
        )
    }

    val playerPower by derivedStateOf {
        val powerDividedBy4 = equippedItems
            .slice(0 until 3)
            .sumOf { it?.power ?: 0.0 }
            .div(4.0)

        val powerDividedBy12 = equippedItems
            .slice(3 until 6)
            .sumOf { it?.power ?: 0.0 }
            .div(12.0)

        (powerDividedBy4 + powerDividedBy12).roundToInt()
    }

    private var xp: Long by mutableStateOf(from.getLong(FIELD_XP))
    var playerLevel: Double
        get() = DungeonsLevel.toInGameLevel(xp).toFixed(3)
        set(value) { xp = DungeonsLevel.toSerializedLevel(value) }

    val totalSpentEnchantmentPoints by derivedStateOf {
        val inventorySum = allItems.sumOf { item -> item.enchantments.sumOf { it.investedPoints } }
        val storageSum = storageItems.sumOf { item -> item.enchantments.sumOf { it.investedPoints } }

        inventorySum + storageSum
    }

    fun export() = JSONObject(from.toString())
        .apply {
            replace(FIELD_ITEMS, allItems.map { it.export() })
            replace(FIELD_STORAGE_CHEST_ITEMS, storageItems.map { it.export() })
            replace(FIELD_CURRENCIES, currencies.map { it.export() })
            replace(FIELD_XP, xp)
        }

    companion object {
        const val FIELD_CURRENCIES = "currency"
        const val FIELD_ITEMS = "items"
        private const val FIELD_NAME = "name"
        private const val FIELD_STORAGE_CHEST_ITEMS = "storageChestItems"
        const val FIELD_XP = "xp"
    }

    @Stable
    class Currency(
        type: String,
        count: Int
    ) {
        companion object {
            private const val FIELD_TYPE = "type"
            private const val FIELD_COUNT = "count"
        }

        var type: String by mutableStateOf(type)
        var count: Int by mutableStateOf(count)

        constructor(from: JSONObject): this(
            type = from.getString(FIELD_TYPE),
            count = from.getInt(FIELD_COUNT)
        )

        fun export(): JSONObject =
            JSONObject().apply {
                put(FIELD_TYPE, type)
                put(FIELD_COUNT, count)
            }
    }

    @Stable
    class Item(
        type: String,
        power: Double,
        rarity: DungeonsItem.Rarity,
        inventoryIndex: Int?,
        equipmentSlot: DungeonsItem.EquipmentSlot? = null,
        netheriteEnchant: Enchantment? = null,
        enchantments: List<Enchantment>? = null,
        armorProperties: List<ArmorProperty>? = null,
        modified: Boolean? = null,
        timesModified: Int? = null,
        upgraded: Boolean,
        markedNew: Boolean? = null
    ) {
        val internalId = UUID.randomUUID().toString()
        var type by mutableStateOf(type)

        var equipmentSlot by mutableStateOf(equipmentSlot)
        var inventoryIndex by mutableStateOf(inventoryIndex)

        private var _power by mutableStateOf(power)
        var power: Double
            get() = DungeonsPower.toInGamePower(_power)
            set(value) { _power = DungeonsPower.toSerializedPower(value) }

        var rarity by mutableStateOf(rarity)

        var upgraded by mutableStateOf(upgraded)

        val enchantments: SnapshotStateList<Enchantment> =
            (enchantments ?: emptyList())
                .padEnd(9) { Enchantment() }
                .toMutableStateList()

        var netheriteEnchant by mutableStateOf(netheriteEnchant)
        val totalEnchantmentInvestedPoints by derivedStateOf { this.enchantments.sumOf { it.investedPoints } }
        val enchanted get() = totalEnchantmentInvestedPoints > 0
        val glided get() = netheriteEnchant != null && netheriteEnchant?.id != "Unset"

        val armorProperties: SnapshotStateList<ArmorProperty> =
            armorProperties
                ?.toMutableStateList()
                ?: mutableStateListOf()

        var modified by mutableStateOf(modified)
        var timesModified by mutableStateOf(timesModified)

        var markedNew by mutableStateOf(markedNew)

        constructor(from: JSONObject): this(
            inventoryIndex = from.tryOrNull { getInt(FIELD_INVENTORY_INDEX) },
            power = from.getDouble(FIELD_POWER),
            rarity = DungeonsItem.Rarity.fromSerialized(from.getString(FIELD_RARITY)),
            type = from.getString(FIELD_TYPE),
            upgraded = from.getBoolean(FIELD_UPGRADED),
            enchantments = from.tryOrNull { getJSONArray(FIELD_ENCHANTMENTS).transformWithJsonObject { Enchantment(it, false) } },
            armorProperties = from.tryOrNull { getJSONArray(FIELD_ARMOR_PROPERTIES).transformWithJsonObject { ArmorProperty(it) } },
            netheriteEnchant = from.tryOrNull { Enchantment(getJSONObject(FIELD_NETHERITE_ENCHANT), true) },
            modified = from.tryOrNull { getBoolean(FIELD_MODIFIED) },
            timesModified = from.tryOrNull { getInt(FIELD_TIMES_MODIFIED) },
            equipmentSlot = DungeonsItem.EquipmentSlot.fromSerialized(from.tryOrNull { getString(FIELD_EQUIPMENT_SLOT) }),
            markedNew = from.tryOrNull { getBoolean(FIELD_MARKED_NEW) }
        )

        fun copy() = Item(
            power = _power,
            rarity = rarity,
            type = type,
            upgraded = upgraded,
            enchantments = enchantments.map { it.copy() },
            armorProperties = armorProperties.map { it.copy() },
            netheriteEnchant = netheriteEnchant?.copy(),
            modified = modified,
            timesModified = timesModified,
            inventoryIndex = 0,
            equipmentSlot = null,
            markedNew = true
        )

        fun export() =
            JSONObject().apply {
                put(FIELD_TYPE, type)
                put(FIELD_POWER, _power)
                put(FIELD_RARITY, rarity)
                put(FIELD_UPGRADED, upgraded)

                inventoryIndex?.let { put(FIELD_INVENTORY_INDEX, it) }
                equipmentSlot?.let { put(FIELD_EQUIPMENT_SLOT, it) }

                modified?.let { put(FIELD_MODIFIED, it) }
                timesModified?.let { put(FIELD_TIMES_MODIFIED, it) }
                markedNew?.let { put(FIELD_MARKED_NEW, it) }

                val exportedArmorProperties = armorProperties.map { property -> property.export() }
                if (exportedArmorProperties.isNotEmpty())
                    put(FIELD_ARMOR_PROPERTIES, exportedArmorProperties)

                netheriteEnchant?.let { if (it.id != "Unset") put(FIELD_NETHERITE_ENCHANT, it.export()) }

                val exportedEnchantments = enchantments
                    .chunked(3)
                    .filter { slot -> slot.any { enchantment -> enchantment.isValid() } }
                    .flatten()
                    .map { enchantment -> enchantment.export() }
                if (exportedEnchantments.isNotEmpty())
                    put(FIELD_ENCHANTMENTS, exportedEnchantments)
            }

        companion object {
            private const val FIELD_INVENTORY_INDEX = "inventoryIndex"
            private const val FIELD_POWER = "power"
            private const val FIELD_RARITY = "rarity"
            private const val FIELD_TYPE = "type"
            private const val FIELD_UPGRADED = "upgraded"
            private const val FIELD_ENCHANTMENTS = "enchantments"
            private const val FIELD_ARMOR_PROPERTIES = "armorproperties"
            private const val FIELD_NETHERITE_ENCHANT = "netheriteEnchant"
            private const val FIELD_MODIFIED = "modified"
            private const val FIELD_TIMES_MODIFIED = "timesmodified"
            private const val FIELD_EQUIPMENT_SLOT = "equipmentSlot"
            private const val FIELD_MARKED_NEW = "markedNew"
        }
    }

    @Stable
    class Enchantment(
        id: String = "Unset",
        level: Int = 0,
        investedPoints: Int = 0,
        val isNetheriteEnchant: Boolean = false
    ) {
        var id: String by mutableStateOf(id)

        var level: Int by mutableStateOf(level)
        var investedPoints: Int by mutableStateOf(investedPoints)

        fun isValid() = id != "Unset"
        fun isNotValid() = id == "Unset"

        constructor(
            from: JSONObject,
            isNetheriteEnchant: Boolean
        ): this(
            id = from.getString(FIELD_ID),
            level = from.getInt(FIELD_LEVEL),
            investedPoints = from.getInt(FIELD_INVESTED_POINTS),
            isNetheriteEnchant = isNetheriteEnchant,
        )

        fun copy(
            id: String = this.id,
            level: Int = this.level,
            investedPoints: Int = this.investedPoints,
            isNetheriteEnchant: Boolean = this.isNetheriteEnchant,
        ) = Enchantment(
            id = id,
            level = level,
            investedPoints = investedPoints,
            isNetheriteEnchant = isNetheriteEnchant
        )

        fun export() =
            JSONObject().apply {
                put(FIELD_ID, id)
                put(FIELD_INVESTED_POINTS, investedPoints)
                put(FIELD_LEVEL, level)
            }

        companion object {
            private const val FIELD_ID = "id"
            private const val FIELD_INVESTED_POINTS = "investedPoints"
            private const val FIELD_LEVEL = "level"
        }
    }

    @Stable
    class ArmorProperty(
        id: String,
        rarity: DungeonsArmorProperty.Rarity = DungeonsArmorProperty.Rarity.Common
    ) {
        var id by mutableStateOf(id)
        var rarity by mutableStateOf(rarity)

        constructor(from: JSONObject): this(
            from.getString(FIELD_ID),
            DungeonsArmorProperty.Rarity.fromSerialized(from.getString(FIELD_RARITY))
        )

        fun copy() = ArmorProperty(
            id = id,
            rarity = rarity
        )

        fun export() =
            JSONObject().apply {
                put(FIELD_ID, id)
                put(FIELD_RARITY, rarity)
            }

        companion object {
            private const val FIELD_ID = "id"
            private const val FIELD_RARITY = "rarity"
        }
    }


}