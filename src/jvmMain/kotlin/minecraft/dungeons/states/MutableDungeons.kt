package minecraft.dungeons.states

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import minecraft.dungeons.resources.DungeonsTower
import minecraft.dungeons.values.*
import org.json.JSONArray
import org.json.JSONObject
import utils.*
import java.util.*
import kotlin.random.Random
import kotlin.uuid.Uuid

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
            .filterNotNull()
            .sumOf { it.power.value }
            .div(4.0)

        val powerDividedBy12 = equippedItems
            .slice(3 until 6)
            .filterNotNull()
            .sumOf { it.power.value }
            .div(12.0)

        (powerDividedBy4 + powerDividedBy12).asInGamePower()
    }

    private var xp: SerializedDungeonsLevel by mutableStateOf(from.getLong(FIELD_XP).asSerializedLevel())
    var playerLevel: InGameDungeonsLevel
        get() = xp.toInGame()
        set(value) { xp = value.toSerialized() }

    val totalSpentEnchantmentPoints by derivedStateOf {
        val inventorySum = allItems.sumOf { item -> item.enchantments.sumOf { it.investedPoints } }
        val storageSum = storageItems.sumOf { item -> item.enchantments.sumOf { it.investedPoints } }

        inventorySum + storageSum
    }

    val uniqueSaveId = from.getString(FIELD_UNIQUE_SAVE_ID)

    val hasInitialTower = from.getJSONObject(FIELD_MISSION_STATES_MAP)?.has(FIELD_TOWER_STATES) ?: false
    var includeEditedTower by mutableStateOf(false)
    var tower by mutableStateOf(
        from
            .tryOrNull { getJSONObject(FIELD_MISSION_STATES_MAP) }
            ?.tryOrNull { getJSONObject(FIELD_TOWER_STATES) }
            ?.tryOrNull { getJSONArray(FIELD_MISSION_STATES) }
            ?.tryOrNull { getJSONObject(0) }
            ?.let { TowerMissionState(it) }
    )

    fun export() = JSONObject(from.toString())
        .apply {
            replace(FIELD_ITEMS, allItems.map { it.export() })
            replace(FIELD_STORAGE_CHEST_ITEMS, storageItems.map { it.export() })
            replace(FIELD_CURRENCIES, currencies.map { it.export() })
            replace(FIELD_XP, xp.value)

            if (!includeEditedTower) return@apply

            val capturedTower = tower
            if (capturedTower == null) {
                getJSONObject(FIELD_MISSION_STATES_MAP).remove(FIELD_TOWER_STATES)
            } else {
                val towerStates = JSONObject()
                val missionStates = JSONArray().apply { put(capturedTower.export()) }
                towerStates.put(FIELD_MISSION_STATES, missionStates)
                getJSONObject(FIELD_MISSION_STATES_MAP).replace(FIELD_TOWER_STATES, towerStates)
            }
        }

    companion object {
        const val FIELD_CURRENCIES = "currency"
        const val FIELD_ITEMS = "items"
        const val FIELD_XP = "xp"

        private const val FIELD_UNIQUE_SAVE_ID = "uniqueSaveId"

        private const val FIELD_NAME = "name"
        private const val FIELD_STORAGE_CHEST_ITEMS = "storageChestItems"

        private const val FIELD_MISSION_STATES_MAP = "missionStatesMap"
        private const val FIELD_MISSION_STATES = "missionStates"
        private const val FIELD_TOWER_STATES = "thetower"
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
        power: SerializedDungeonsPower,
        rarity: DungeonsItem.Rarity,
        inventoryIndex: Int? = null,
        equipmentSlot: DungeonsItem.EquipmentSlot? = null,
        netheriteEnchant: Enchantment? = null,
        enchantments: List<Enchantment>? = null,
        armorProperties: List<ArmorProperty>? = null,
        modified: Boolean? = null,
        timesModified: Int? = null,
        upgraded: Boolean = false,
        markedNew: Boolean? = null
    ) {
        val internalId = UUID.randomUUID().toString()
        var type by mutableStateOf(type)

        var equipmentSlot by mutableStateOf(equipmentSlot)
        var inventoryIndex by mutableStateOf(inventoryIndex)

        private var _power by mutableStateOf(power)
        var power: InGameDungeonsPower
            get() = _power.toInGame()
            set(value) { _power = value.toSerialized() }

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
            power = from.getDouble(FIELD_POWER).asSerializedPower(),
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

        fun copy(markedNew: Boolean = true) = Item(
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
            markedNew = markedNew
        )

        fun export() =
            JSONObject().apply {
                put(FIELD_TYPE, type)
                put(FIELD_POWER, _power.value)
                put(FIELD_RARITY, rarity.serialized)
                put(FIELD_UPGRADED, upgraded)

                inventoryIndex?.let { put(FIELD_INVENTORY_INDEX, it) }
                equipmentSlot?.let { put(FIELD_EQUIPMENT_SLOT, it.serialized) }

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
                put(FIELD_RARITY, rarity.serialized)
            }

        companion object {
            private const val FIELD_ID = "id"
            private const val FIELD_RARITY = "rarity"
        }
    }

    @Stable
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    class TowerMissionState(
        livesLost: Int = 0,
        val guid: String = Uuid.random().toHexString().uppercase(), // Immutable
        val completedOnce: Boolean = false, // Unknown, preserve input or empty, Maybe unused.
        val offeredEnchantmentPoints: Int = 0, // Unknown, preserve input or empty, Maybe unused.
        val seed: Long = 0, // Unknown, preserve input or empty, Maybe unused.
        val partsDiscovered: Int = 0, // Unknown, preserve input or empty, Maybe unused.
        val ownedDLCs: List<*> = emptyList<Any>(), // Unknown, preserve input or empty, Maybe unused.
        val offeredItems: List<*> = emptyList<Any>(),  // Unknown, preserve input or empty, Maybe unused.
        val missionDifficulty: Difficulty = Difficulty(), // State itself
        val towerInfo: Info, // State itself
    ) {

        var livesLost by mutableStateOf(livesLost)

        companion object {
            private const val FIELD_GUID = "guid"
            private const val FIELD_LIVES_LOST = "livesLost"
            private const val FIELD_COMPLETED_ONCE = "completedOnce"
            private const val FIELD_OFFERED_ITEMS = "offeredItems"
            private const val FIELD_SEED = "seed"
            private const val FIELD_PARTS_DISCOVERED = "partsDiscovered"
            private const val FIELD_OFFERED_ENCHANTMENT_POINTS = "offeredEnchantmentPoints"
            private const val FIELD_MISSION_DIFFICULTY = "missionDifficulty"
            private const val FIELD_OWNED_DLCS = "ownedDLCs"
            private const val FIELD_TOWER_INFO = "towerInfo"
        }

        constructor(from: JSONObject): this(
            livesLost = from.getInt(FIELD_LIVES_LOST),
            guid = from.getString(FIELD_GUID),
            completedOnce = from.getBoolean(FIELD_COMPLETED_ONCE),
            offeredEnchantmentPoints = from.getInt(FIELD_OFFERED_ENCHANTMENT_POINTS),
            seed = from.getLong(FIELD_SEED),
            partsDiscovered = from.getInt(FIELD_PARTS_DISCOVERED),
            ownedDLCs = from.getJSONArray(FIELD_OWNED_DLCS).toList(),
            offeredItems = from.getJSONArray(FIELD_OFFERED_ITEMS).toList(),
            missionDifficulty = Difficulty(from.getJSONObject(FIELD_MISSION_DIFFICULTY)),
            towerInfo = Info(from.getJSONObject(FIELD_TOWER_INFO))
        )

        constructor(from: String): this(
            towerInfo = Info(
                towerMobGroupConfig = DungeonsTower.mobGroupConfig, // FIXME!!
                towerPlayersData = listOf(Info.PlayerData(localSaveGUID = from)),
            )
        )

        fun export() = JSONObject().apply {
            put(FIELD_GUID, guid)
            put(FIELD_LIVES_LOST, livesLost)
            put(FIELD_COMPLETED_ONCE, completedOnce)
            put(FIELD_OFFERED_ITEMS, offeredItems)
            put(FIELD_SEED, seed)
            put(FIELD_PARTS_DISCOVERED, partsDiscovered)
            put(FIELD_OFFERED_ENCHANTMENT_POINTS, offeredEnchantmentPoints)
            put(FIELD_MISSION_DIFFICULTY, missionDifficulty.export())
            put(FIELD_OWNED_DLCS, ownedDLCs)
            put(FIELD_TOWER_INFO, towerInfo.export())
        }

        @Stable
        class Difficulty(
            difficulty: Int = 1,
            threatLevel: Int = 1,
            private val endlessStruggle: Int = 0,
        ) {
            var difficulty by mutableStateOf(difficulty)
            var threatLevel by mutableStateOf(threatLevel)

            companion object {
                private const val FIELD_DIFFICULTY = "difficulty"
                private const val FIELD_ENDLESS_STRUGGLE = "endlessStruggle"
                private const val FIELD_MISSION = "mission"
                private const val FIELD_THREAT_LEVEL = "threatLevel"
            }

            constructor(from: JSONObject): this(
                difficulty = from.getString(FIELD_DIFFICULTY).removePrefix("Difficulty_").toInt(),
                threatLevel = from.getString(FIELD_THREAT_LEVEL).removePrefix("Threat_").toInt(),
                endlessStruggle = from.getInt(FIELD_ENDLESS_STRUGGLE)
            )

            fun export() = JSONObject().apply {
                put(FIELD_DIFFICULTY, "Difficulty_$difficulty")
                put(FIELD_ENDLESS_STRUGGLE, endlessStruggle)
                put(FIELD_MISSION, "thetower")
                put(FIELD_THREAT_LEVEL, "Threat_$threatLevel")
            }
        }

        @Stable
        class Info(
            towerCurrentFloorWasCompleted: Boolean = false,
            towerFinished: Boolean = false,
            towerPlayersData: List<PlayerData>,

            val towerId: Uuid = Uuid.random(), // Immutable
            val towerMobGroupConfig: JSONObject, // From thetower.json
            val towerFinalRewards: List<*> = emptyList<Any>(), // Unknown, Preserve input or empty
            val towerOfferedFloorRewards: Map<String, Any> = emptyMap(), // Unknown, Preserve input or empty
            val towerConfig: Config = Config(), // State itself
            val towerInfo: InnerInfo = InnerInfo(), // State itself
        ) {

            var towerCurrentFloorWasCompleted by mutableStateOf(towerCurrentFloorWasCompleted)
            var towerFinished by mutableStateOf(towerFinished)
            val towerPlayersData = towerPlayersData.toMutableStateList()

            companion object {
                private const val FIELD_TOWER_ID = "towerId"
                private const val FIELD_TOWER_FINAL_REWARDS = "towerFinalRewards"
                private const val FIELD_TOWER_MOB_GROUP_CONFIG = "towerMobGroupConfig"
                private const val FIELD_TOWER_OFFERED_FLOOR_REWARDS = "towerOfferedFloorRewards"
                private const val FIELD_TOWER_CONFIG = "towerConfig"
                private const val FIELD_TOWER_INFO = "towerInfo"
                private const val FIELD_TOWER_CURRENT_FLOOR_WAS_COMPLETED = "towerCurrentFloorWasCompleted"
                private const val FIELD_TOWER_FINISHED = "towerFinished"
                private const val FIELD_TOWER_PLAYERS_DATA = "towerPlayersData"
            }

            constructor(from: JSONObject): this(
                towerCurrentFloorWasCompleted = from.getBoolean(FIELD_TOWER_CURRENT_FLOOR_WAS_COMPLETED),
                towerFinished = from.getBoolean(FIELD_TOWER_FINISHED),
                towerPlayersData = from.getJSONArray(FIELD_TOWER_PLAYERS_DATA).transformWithJsonObject { PlayerData(it) },
                towerId = Uuid.parseHexDash(from.getString(FIELD_TOWER_ID)),
                towerMobGroupConfig = from.getJSONObject(FIELD_TOWER_MOB_GROUP_CONFIG),
                towerFinalRewards = from.getJSONArray(FIELD_TOWER_FINAL_REWARDS).toList(),
                towerOfferedFloorRewards = from.getJSONObject(FIELD_TOWER_OFFERED_FLOOR_REWARDS).toMap(),
                towerConfig = Config(from.getJSONObject(FIELD_TOWER_CONFIG)),
                towerInfo = InnerInfo(from.getJSONObject(FIELD_TOWER_INFO))
            )

            fun export() = JSONObject().apply {
                put(FIELD_TOWER_ID, towerId.toHexDashString())
                put(FIELD_TOWER_FINAL_REWARDS, towerFinalRewards)
                put(FIELD_TOWER_MOB_GROUP_CONFIG, towerMobGroupConfig)
                put(FIELD_TOWER_OFFERED_FLOOR_REWARDS, towerOfferedFloorRewards)
                put(FIELD_TOWER_CONFIG, towerConfig.export())
                put(FIELD_TOWER_INFO, towerInfo.export())
                put(FIELD_TOWER_CURRENT_FLOOR_WAS_COMPLETED, towerCurrentFloorWasCompleted)
                put(FIELD_TOWER_FINISHED, towerFinished)
                put(FIELD_TOWER_PLAYERS_DATA, towerPlayersData.map { it.export() })
            }

            class PlayerData(
                val localSaveGUID: String,
                val playerId: Long = Random.nextLong(),
                val playerMerchantInteractions: List<Any> = emptyList(),
                val playerLocalId: Long = Random.nextLong(),
                playerArrowsAmmount: Int = 100,
                playerEnchantmentPointsGranted: Int = 3,
                playerIsTowerOwner: Boolean = true,
                playerLastFloorIndex: Int = 0,
                playerItems: List<Item> = DefaultPlayerItems
            ) {
                var playerArrowsAmmount by mutableStateOf(playerArrowsAmmount)
                var playerEnchantmentPointsGranted by mutableStateOf(playerEnchantmentPointsGranted)
                var playerIsTowerOwner by mutableStateOf(playerIsTowerOwner)
                var playerLastFloorIndex by mutableStateOf(playerLastFloorIndex)
                val playerItems = playerItems.toMutableStateList()

                companion object {
                    private val DefaultPlayerItems = listOf(
                        Item("Sword", 1.0.asSerializedPower(), DungeonsItem.Rarity.Common),
                        Item("MercenaryArmor", 1.0.asSerializedPower(), DungeonsItem.Rarity.Common),
                        Item("Bow", 1.0.asSerializedPower(), DungeonsItem.Rarity.Common),
                    )

                    private const val FIELD_LOCAL_SAVE_GUID = "localSaveGUID"
                    private const val FIELD_ARROWS_AMOUNT = "playerArrowsAmmount"
                    private const val FIELD_ENCHANTMENT_POINTS_GRANTED = "playerEnchantmentPointsGranted"
                    private const val FIELD_PLAYER_ID = "playerId"
                    private const val FIELD_PLAYER_IS_TOWER_OWNER = "playerIsTowerOwner"
                    private const val FIELD_LAST_FLOOR_INDEX = "playerLastFloorIndex"
                    private const val FIELD_LOCAL_ID = "playerLocalId"
                    private const val FIELD_PLAYER_ITEMS = "playerItems"
                    private const val FIELD_PLAYER_MERCHANT_INTERACTIONS = "playerMerchantInteractions"
                }

                constructor(from: JSONObject): this(
                    localSaveGUID = from.getString(FIELD_LOCAL_SAVE_GUID),
                    playerId = from.getLong(FIELD_PLAYER_ID),
                    playerMerchantInteractions = from.getJSONArray(FIELD_PLAYER_MERCHANT_INTERACTIONS).toList(),
                    playerArrowsAmmount = from.getInt(FIELD_ARROWS_AMOUNT),
                    playerEnchantmentPointsGranted = from.getInt(FIELD_ENCHANTMENT_POINTS_GRANTED),
                    playerIsTowerOwner = from.getBoolean(FIELD_PLAYER_IS_TOWER_OWNER),
                    playerLastFloorIndex = from.getInt(FIELD_LAST_FLOOR_INDEX),
                    playerLocalId = from.getLong(FIELD_LOCAL_ID),
                    playerItems = from.getJSONArray(FIELD_PLAYER_ITEMS).transformWithJsonObject { Item(it) }
                )

                fun export() = JSONObject().apply {
                    put(FIELD_LOCAL_SAVE_GUID, localSaveGUID)
                    put(FIELD_ARROWS_AMOUNT, playerArrowsAmmount)
                    put(FIELD_ENCHANTMENT_POINTS_GRANTED, playerEnchantmentPointsGranted)
                    put(FIELD_PLAYER_ID, playerId)
                    put(FIELD_PLAYER_IS_TOWER_OWNER, playerIsTowerOwner)
                    put(FIELD_LAST_FLOOR_INDEX, playerLastFloorIndex)
                    put(FIELD_LOCAL_ID, playerLocalId)
                    put(FIELD_PLAYER_ITEMS, playerItems.map { it.export() })
                    put(FIELD_PLAYER_MERCHANT_INTERACTIONS, playerMerchantInteractions)
                }
            }

            class InnerInfo(
                towerInfoBossesKilled: Int = 0,
                towerInfoCurrentFloor: Int = 0,
                towerInfoFloors: List<Floor> = List(31) { Floor() }
            ) {

                var towerInfoBossesKilled by mutableStateOf(towerInfoBossesKilled)
                var towerInfoCurrentFloor by mutableStateOf(towerInfoCurrentFloor)
                val towerInfoFloors = towerInfoFloors.toMutableStateList()

                companion object {
                    private const val FIELD_TOWER_INFO_BOSSES_KILLED = "towerInfoBossesKilled"
                    private const val FIELD_TOWER_INFO_CURRENT_FLOOR = "towerInfoCurrentFloor"
                    private const val FIELD_TOWER_INFO_FLOORS = "towerInfoFloors"
                }

                constructor(from: JSONObject): this(
                    towerInfoBossesKilled = from.getInt(FIELD_TOWER_INFO_BOSSES_KILLED),
                    towerInfoCurrentFloor = from.getInt(FIELD_TOWER_INFO_CURRENT_FLOOR),
                    towerInfoFloors = from.getJSONArray(FIELD_TOWER_INFO_FLOORS).transformWithJsonObject { Floor(it) }
                )

                fun export() = JSONObject().apply {
                    put(FIELD_TOWER_INFO_BOSSES_KILLED, towerInfoBossesKilled)
                    put(FIELD_TOWER_INFO_CURRENT_FLOOR, towerInfoCurrentFloor)
                    put(FIELD_TOWER_INFO_FLOORS, towerInfoFloors.map { it.export() })
                }

                class Floor(
                    towerFloorType: Type = Type.Empty
                ) {
                    var towerFloorType by mutableStateOf(towerFloorType)

                    companion object {
                        private const val FIELD_TOWER_FLOOR_TYPE = "towerFloorType"
                    }

                    constructor(from: JSONObject): this(
                        towerFloorType = Type.fromString(from.getString(FIELD_TOWER_FLOOR_TYPE))
                    )

                    fun export() = JSONObject().apply {
                        put("towerFloorType", towerFloorType.value)
                    }

                    enum class Type(val value: String) {
                        Empty("Empty"),
                        Combat("Combat"),
                        Merchant("Merchant"),
                        Boss("Boss");

                        companion object {
                            private val valueMap = Type.entries.associateBy(Type::value)
                            fun fromString(value: String) = valueMap.getValue(value)
                        }
                    }
                }
            }

            @Stable
            class Config(
                floors: List<Floor> = List(31) { Floor() },
                seed: Long = 0L
            ) {
                val floors = floors.toMutableStateList()
                var seed by mutableStateOf(seed)

                companion object {
                    private const val FIELD_FLOORS = "floors"
                    private const val FIELD_SEED = "seed"
                }

                constructor(from: JSONObject): this(
                    floors = from.getJSONArray("floors").transformWithJsonObject { Floor(it) },
                    seed = from.getLong("seed"),
                )

                fun export() = JSONObject().apply {
                    put(FIELD_FLOORS, floors.map { it.export() })
                    put(FIELD_SEED, seed)
                }

                @Stable
                class Floor(
                    rewards: List<Reward> = List(5) { Reward.Any },
                    challenges: List<String>? = null,
                    tile: String = ""
                ) {
                    val rewards = rewards.toMutableStateList()
                    val challenges = challenges?.toMutableStateList() ?: mutableStateListOf()
                    var tile by mutableStateOf(tile)

                    companion object {
                        private const val FIELD_CHALLENGES = "challenges"
                        private const val FIELD_REWARDS = "rewards"
                        private const val FIELD_TILE = "tile"
                        private const val FIELD_TYPE = "type"
                    }

                    constructor(from: JSONObject): this(
                        rewards = from.getJSONArray(FIELD_REWARDS).toStringList().map { Reward.fromString(it) },
                        challenges = Unit.runCatching { from.getJSONArray(FIELD_CHALLENGES).toStringList() }.getOrNull(),
                        tile = from.getString(FIELD_TILE)
                    )

                    fun export() = JSONObject().apply {
                        if (challenges.isNotEmpty())
                            put(FIELD_CHALLENGES, challenges)
                        put(FIELD_REWARDS, rewards.map { it.value })
                        put(FIELD_TILE, tile)
                        put(FIELD_TYPE, "")
                    }

                    enum class Reward(val value: String) {
                        Any("any"),
                        Melee("melee"),
                        Ranged("ranged"),
                        Armor("armor"),
                        Artifact("artefact");

                        fun next() = Reward.entries[(Reward.entries.indexOf(this) + 1) % Reward.entries.size]

                        companion object {
                            private val valueMap = Reward.entries.associateBy(Reward::value)
                            fun fromString(value: String) = valueMap.getValue(value)
                        }
                    }
                }
            }
        }

    }

}