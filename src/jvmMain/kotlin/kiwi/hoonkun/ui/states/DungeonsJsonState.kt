package kiwi.hoonkun.ui.states

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import extensions.toFixed
import kiwi.hoonkun.utils.*
import minecraft.dungeons.io.DungeonsJsonFile
import minecraft.dungeons.resources.DungeonsDatabase
import minecraft.dungeons.resources.EnchantmentData
import minecraft.dungeons.values.DungeonsLevel
import minecraft.dungeons.values.DungeonsPower
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.util.*

@Target(AnnotationTarget.PROPERTY)
annotation class CanBeUndefined

@Target(AnnotationTarget.PROPERTY)
annotation class JsonField(val name: String)

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class MustBeVerified(val message: String)

@Stable
class DungeonsJsonState(private val from: JSONObject, private val source: File) {

    companion object {
        private const val FIELD_BONUS_PREREQUISITES = "bonus_prerequisites"
        private const val FIELD_CLONE = "clone"
        private const val FIELD_COSMETICS = "cosmetics"
        private const val FIELD_COSMETICS_EVER_EQUIPPED = "cosmeticsEverEquipped"
        private const val FIELD_CREATION_DATE = "creationDate"
        private const val FIELD_CURRENCIES_FOUND = "currenciesFound"
        private const val FIELD_CURRENCIES = "currency"
        private const val FIELD_CUSTOMIZED = "customized"
        private const val FIELD_DIFFICULTIES = "difficulties"
        private const val FIELD_END_GAME_CONTENT_PROGRESS = "endGameContentProgress"
        private const val FIELD_FINISHED_OBJECTIVE_TAGS = "finishedObjectiveTags"
        private const val FIELD_ITEMS = "items"
        private const val FIELD_ITEMS_FOUND = "itemsFound"
        private const val FIELD_LEGENDARY_STATUS = "legendaryStatus"
        private const val FIELD_LOBBY_CHEST_PROGRESS = "lobbychest_progress"
        private const val FIELD_MAP_UI_STATE = "mapUIState"
        private const val FIELD_MERCHANT_DATA = "merchantData"
        private const val FIELD_MOB_KILLS = "mob_kills"
        private const val FIELD_MISSION_STATES_MAP = "missionStatesMap"
        private const val FIELD_NAME = "name"
        private const val FIELD_PENDING_REWARD_ITEMS = "pendingRewardItems"
        private const val FIELD_PLAYER_ID = "playerId"
        private const val FIELD_STAGE_PROGRESS = "progress"
        private const val FIELD_STAGE_PROGRESS_STAT_COUNTERS = "progressStatCounters"
        private const val FIELD_PROGRESSION_KEYS = "progressionKeys"
        private const val FIELD_SKIN = "skin"
        private const val FIELD_STORAGE_CHEST_ANNOUNCEMENT = "storageChestAnnouncement"
        private const val FIELD_STORAGE_CHEST_ITEMS = "storageChestItems"
        private const val FIELD_STRONGHOLD_PROGRESS = "strongholdProgress"
        private const val FIELD_THREAT_LEVELS = "threatLevels"
        private const val FIELD_TIMESTAMP = "timestamp"
        private const val FIELD_TOTAL_GEAR_POWER = "totalGearPower"
        private const val FIELD_TRIALS_COMPLETED = "trialsCompleted"
        private const val FIELD_UI_HINTS_EXPIRED = "uiHintsExpired"
        private const val FIELD_UNIQUE_SAVE_ID = "uniqueSaveId"
        private const val FIELD_VERSION = "version"
        private const val FIELD_VIDEOS_PLAYED = "videosPlayed"
        private const val FIELD_XP = "xp"
    }

    val sourcePath get() = source.absolutePath

    @JsonField(FIELD_BONUS_PREREQUISITES)
    val bonusPrerequisites: SnapshotStateList<String> = from.getJSONArray(FIELD_BONUS_PREREQUISITES).toStringList().toMutableStateList()

    @JsonField(FIELD_CLONE)
    var clone: Boolean by mutableStateOf(from.getBoolean(FIELD_CLONE))

    @JsonField(FIELD_COSMETICS)
    val cosmetics: SnapshotStateList<Cosmetic> = from.getJSONArray(FIELD_COSMETICS).transformWithJsonObject { Cosmetic(it) }.toMutableStateList()

    @JsonField(FIELD_COSMETICS_EVER_EQUIPPED)
    val cosmeticsEverEquipped: SnapshotStateList<CosmeticId> = from.getJSONArray(FIELD_COSMETICS_EVER_EQUIPPED).toStringList().toMutableStateList()

    @JsonField(FIELD_CREATION_DATE)
    var creationDate: String by mutableStateOf(from.getString(FIELD_CREATION_DATE))

    @JsonField(FIELD_CURRENCIES_FOUND)
    val currenciesFound: SnapshotStateList<CurrencyId> = from.getJSONArray(FIELD_CURRENCIES_FOUND).toStringList().toMutableStateList()

    @JsonField(FIELD_CURRENCIES)
    val currencies: SnapshotStateList<Currency> = from.getJSONArray(FIELD_CURRENCIES).transformWithJsonObject { Currency(it) }.toMutableStateList()

    @JsonField(FIELD_CUSTOMIZED)
    var customized: Boolean by mutableStateOf(from.getBoolean(FIELD_CUSTOMIZED))

    @JsonField(FIELD_DIFFICULTIES)
    val difficulties: Difficulties? by mutableStateOf(from.safe { Difficulties(getJSONObject(FIELD_DIFFICULTIES)) })

    @JsonField(FIELD_END_GAME_CONTENT_PROGRESS)
    val endgameContentProgress: EndGameContentProgress =
        EndGameContentProgress(from.getJSONObject(FIELD_END_GAME_CONTENT_PROGRESS))

    @JsonField(FIELD_FINISHED_OBJECTIVE_TAGS)
    var finishedObjectiveTags: FinishedObjectiveTags? by mutableStateOf(
        from.safe {
            FinishedObjectiveTags(getJSONObject(
                FIELD_FINISHED_OBJECTIVE_TAGS
            ))
        }
    )

    @JsonField(FIELD_ITEMS)
    val items: SnapshotStateList<Item> = from.getJSONArray(FIELD_ITEMS).transformWithJsonObject { Item(this@DungeonsJsonState, it) }.toMutableStateList()

    @JsonField(FIELD_ITEMS_FOUND)
    val itemsFound: SnapshotStateList<ItemType> = from.getJSONArray(FIELD_ITEMS_FOUND).toStringList().toMutableStateList()

    @JsonField(FIELD_LEGENDARY_STATUS)
    var legendaryStatus: Int by mutableStateOf(from.getInt(FIELD_LEGENDARY_STATUS))

    @JsonField(FIELD_LOBBY_CHEST_PROGRESS)
    var lobbyCanBeUndefined: SnapshotStateList<LobbyChestProgress>? by mutableStateOf(
        from.safe {
            getJSONArray(FIELD_LOBBY_CHEST_PROGRESS)
                .transformWithJsonObject { LobbyChestProgress(it) }
                .toMutableStateList()
        }
    )

    @JsonField(FIELD_MAP_UI_STATE)
    var mapUiState: MapUiState? by mutableStateOf(from.safe { MapUiState(getJSONObject(FIELD_MAP_UI_STATE)) })

    @JsonField(FIELD_MERCHANT_DATA)
    val merchantData: MerchantDataSet = MerchantDataSet(this@DungeonsJsonState, from.getJSONObject(FIELD_MERCHANT_DATA))

    @JsonField(FIELD_MOB_KILLS)
    var mobKills: SnapshotStateMap<String, Int>? by mutableStateOf(
        from.safe { getJSONObject(FIELD_MOB_KILLS).toIntMap().toMutableStateMap() }
    )

    @JsonField(FIELD_MISSION_STATES_MAP)
    val missionStatesMap: SnapshotStateMap<String, MissionStatesHolder> = from.getJSONObject(FIELD_MISSION_STATES_MAP)
        .transformWithJsonObject { MissionStatesHolder(it) }
        .toMutableStateMap()

    @JsonField(FIELD_NAME)
    var name: String by mutableStateOf(from.getString(FIELD_NAME))

    @JsonField(FIELD_PENDING_REWARD_ITEMS)
    val pendingRewardItems: SnapshotStateList<Item> = from.getJSONArray(FIELD_PENDING_REWARD_ITEMS)
        .transformWithJsonObject { Item(this@DungeonsJsonState, it) }
        .toMutableStateList()

    @JsonField(FIELD_PLAYER_ID)
    var playerId: String by mutableStateOf(from.getString(FIELD_PLAYER_ID))

    @JsonField(FIELD_STAGE_PROGRESS)
    var progress: SnapshotStateMap<String, StageProgress>? by mutableStateOf(
        from.safe {
            getJSONObject(FIELD_STAGE_PROGRESS)
                .transformWithJsonObject { StageProgress(it) }
                .toMutableStateMap()
        }
    )

    @JsonField(FIELD_STAGE_PROGRESS_STAT_COUNTERS)
    val progressStatCounters: SnapshotStateMap<String, Int> = from.getJSONObject(FIELD_STAGE_PROGRESS_STAT_COUNTERS)
        .toIntMap()
        .toMutableStateMap()

    @JsonField(FIELD_PROGRESSION_KEYS)
    val progressionKeys: SnapshotStateList<String> = from.getJSONArray(FIELD_PROGRESSION_KEYS)
        .toStringList()
        .toMutableStateList()

    @JsonField(FIELD_SKIN)
    var skin: String by mutableStateOf(from.getString(FIELD_SKIN))

    @JsonField(FIELD_STORAGE_CHEST_ANNOUNCEMENT)
    val storageChestAnnouncement: StorageChestAnnouncement? by mutableStateOf(
        from.safe { StorageChestAnnouncement(getJSONObject(FIELD_STORAGE_CHEST_ANNOUNCEMENT)) }
    )

    @JsonField(FIELD_STORAGE_CHEST_ITEMS)
    val storageChestItems: SnapshotStateList<Item> = from.getJSONArray(FIELD_STORAGE_CHEST_ITEMS).transformWithJsonObject { Item(this@DungeonsJsonState, it) }.toMutableStateList()

    @JsonField(FIELD_STRONGHOLD_PROGRESS)
    val strongholdProgress: SnapshotStateMap<String, Boolean> = from.getJSONObject(FIELD_STRONGHOLD_PROGRESS).toBooleanMap().toMutableStateMap()

    @JsonField(FIELD_THREAT_LEVELS)
    var threatLevels: ThreatLevels? by mutableStateOf(from.safe { ThreatLevels(getJSONObject(FIELD_THREAT_LEVELS)) })

    @JsonField(FIELD_TIMESTAMP)
    var timestamp: Long by mutableStateOf(from.getLong(FIELD_TIMESTAMP))

    @JsonField(FIELD_TOTAL_GEAR_POWER)
    var totalGearPower: Int by mutableStateOf(from.getInt(FIELD_TOTAL_GEAR_POWER))

    @JsonField(FIELD_TRIALS_COMPLETED)
    val trialsCompleted: SnapshotStateList<CompletedTrial> = from.getJSONArray(FIELD_TRIALS_COMPLETED)
        .transformWithJsonObject { CompletedTrial(it) }
        .toMutableStateList()

    @JsonField(FIELD_UI_HINTS_EXPIRED)
    val uiHintsExpired: SnapshotStateList<UiHint> = from.getJSONArray(FIELD_UI_HINTS_EXPIRED)
        .transformWithJsonObject { UiHint(it) }
        .toMutableStateList()

    @JsonField(FIELD_UNIQUE_SAVE_ID)
    var uniqueSaveId: String by mutableStateOf(from.getString(FIELD_UNIQUE_SAVE_ID))

    @JsonField(FIELD_VERSION)
    var version: Int by mutableStateOf(from.getInt(FIELD_VERSION))

    @JsonField(FIELD_VIDEOS_PLAYED)
    val videosPlayed: SnapshotStateList<String> = from.getJSONArray(FIELD_VIDEOS_PLAYED)
        .toStringList()
        .toMutableStateList()

    @JsonField(FIELD_XP)
    var xp: Long by mutableStateOf(from.getLong(FIELD_XP))


    val equippedMelee by derivedStateOf { items.find { it.equipmentSlot == "MeleeGear" } }
    val equippedRanged by derivedStateOf { items.find { it.equipmentSlot == "RangedGear" } }
    val equippedArmor by derivedStateOf { items.find { it.equipmentSlot == "ArmorGear" } }
    val equippedArtifact1 by derivedStateOf { items.find { it.equipmentSlot == "HotbarSlot1" } }
    val equippedArtifact2 by derivedStateOf { items.find { it.equipmentSlot == "HotbarSlot2" } }
    val equippedArtifact3 by derivedStateOf { items.find { it.equipmentSlot == "HotbarSlot3" } }

    val equippedItems by derivedStateOf {
        listOf(
            equippedMelee, equippedArmor, equippedRanged,
            equippedArtifact1, equippedArtifact2, equippedArtifact3
        )
    }
    val unequippedItems by derivedStateOf { items.filter { it.inventoryIndex != null } }

    val playerPower by derivedStateOf {
        val powerDividedBy4 = listOf(
            equippedMelee,
            equippedArmor,
            equippedRanged
        ).sumOf { DungeonsPower.toInGamePower(it?.power ?: 0.0) } / 4.0

        val powerDividedBy12 = listOf(
            equippedArtifact1,
            equippedArtifact2,
            equippedArtifact3
        ).sumOf { DungeonsPower.toInGamePower(it?.power ?: 0.0) } / 12.0

        (powerDividedBy4 + powerDividedBy12).toInt()
    }

    val playerLevel by derivedStateOf {
        DungeonsLevel.toInGameLevel(xp).toFixed(3)
    }

    val totalSpentEnchantmentPoints by derivedStateOf {
        val inventorySum = items.sumOf { it.enchantments?.sumOf { en -> en.investedPoints } ?: 0 }
        val storageSum = storageChestItems.sumOf { it.enchantments?.sumOf { en -> en.investedPoints } ?: 0 }

        inventorySum + storageSum
    }

    fun export(): JSONObject =
        JSONObject(from.toString()).apply {
            replace(FIELD_ITEMS, items.map { it.export() })
            replace(FIELD_STORAGE_CHEST_ITEMS, storageChestItems.map { it.export() })
            replace(FIELD_CURRENCIES, currencies.map { it.export() })
            replace(FIELD_XP, xp)
        }

    fun save(file: DungeonsJsonFile = DungeonsJsonFile(source), createBackup: Boolean = true) {
        val date = LocalDateTime.now().run {
            val year = year - 2000
            val date = listOf(monthValue, dayOfMonth, hour, minute, second).joinToString("") { "$it".padStart(2, '0') }
            "$year$date"
        }
        if (file.isDirectory) {
            if (createBackup)
                source.copyTo(File("${file.absolutePath}/${source.nameWithoutExtension}.b$date.dat"))
            DungeonsJsonFile("${file.absolutePath}/${source.name}").write(export())
        } else {
            if (createBackup)
                source.copyTo(File("${file.parentFile.absolutePath}/${source.nameWithoutExtension}.b$date.dat"))
            file.write(export())
        }
    }

    fun addItem(editor: EditorState, newItem: Item, copiedFrom: Item? = null) {
        val where = copiedFrom?.where ?: editor.view
        if (where == EditorState.EditorView.Inventory) {
            items.add(6.coerceAtMost(items.size), newItem)
            unequippedItems.forEachIndexed { index, item -> item.inventoryIndex = index }
        } else {
            storageChestItems.add(0, newItem)
            storageChestItems.forEachIndexed { index, item -> item.inventoryIndex = index }
        }

        if (copiedFrom != null) {
            editor.selection.replace(copiedFrom, newItem)
        } else {
            editor.selection.clear()
            editor.selection.select(newItem, EditorState.SelectionState.Slot.Primary)
        }
    }

    fun deleteItem(editor: EditorState, targetItem: Item) {
        editor.selection.unselect(targetItem)

        when (targetItem.where) {
            EditorState.EditorView.Inventory -> {
                items.remove(targetItem)
                unequippedItems.forEachIndexed { index, item -> item.inventoryIndex = index }
            }
            EditorState.EditorView.Storage -> {
                storageChestItems.remove(targetItem)
                storageChestItems.forEachIndexed { index, item -> item.inventoryIndex = index }
            }
            else -> { /* This item does not exist in any available spaces. */ }
        }
    }


}

typealias CosmeticId = String
typealias CurrencyId = String
typealias ItemType = String

@Stable
class Cosmetic(id: String, type: String) {
    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_TYPE = "type"
    }

    @JsonField(FIELD_ID)
    var id: String by mutableStateOf(id)

    @JsonField(FIELD_TYPE)
    var type: String by mutableStateOf(type)

    constructor(from: JSONObject): this(from.getString(FIELD_ID), from.getString(FIELD_TYPE))
}

@Stable
class Currency(type: String, count: Int) {
    companion object {
        private const val FIELD_TYPE = "type"
        private const val FIELD_COUNT = "count"
    }

    @JsonField(FIELD_TYPE)
    var type: String by mutableStateOf(type)

    @JsonField(FIELD_COUNT)
    var count: Int by mutableStateOf(count)

    constructor(from: JSONObject): this(from.getString(FIELD_TYPE), from.getInt(FIELD_COUNT))

    fun export(): JSONObject =
        JSONObject().apply {
            put(FIELD_TYPE, type)
            put(FIELD_COUNT, count)
        }
}

@Stable
class Difficulties(announced: String, selected: String, unlocked: String) {
    companion object {
        private const val FIELD_ANNOUNCED = "announced"
        private const val FIELD_SELECTED = "selected"
        private const val FIELD_UNLOCKED = "unlocked"
    }

    @JsonField(FIELD_ANNOUNCED)
    var announced: String by mutableStateOf(announced)

    @JsonField(FIELD_SELECTED)
    var selected: String by mutableStateOf(selected)

    @JsonField(FIELD_UNLOCKED)
    var unlocked: String by mutableStateOf(unlocked)

    constructor(from: JSONObject): this(
        from.getString(FIELD_ANNOUNCED),
        from.getString(FIELD_SELECTED),
        from.getString(FIELD_UNLOCKED)
    )
}

@Stable
class EndGameContentProgress(announcedUnlockedContent: List<String>) {
    companion object {
        private const val FIELD_ANNOUNCED_UNLOCKED_CONTENT = "announcedUnlockedContent"
    }

    @JsonField(FIELD_ANNOUNCED_UNLOCKED_CONTENT)
    val announcedUnlockedContent = announcedUnlockedContent.toMutableStateList()

    constructor(from: JSONObject): this(from.getJSONArray(FIELD_ANNOUNCED_UNLOCKED_CONTENT).toStringList())
}

@Stable
@MustBeVerified("Is there any fields which can be undefined?")
class FinishedObjectiveTags(decorActor: Int, rescuedVillager: Int, cannotAttachProjectiles: Int) {
    companion object {
        private const val FIELD_DECOR_ACTOR = "LovikaDecorActor"
        private const val FIELD_RESCUED_VILLAGER = "Objective_RescuedVillager"
        private const val FIELD_CANNOT_ATTACH_PROJECTILES = "cannot-attach-projectiles"
    }

    @JsonField(FIELD_DECOR_ACTOR)
    var decorActor: Int by mutableStateOf(decorActor)

    @JsonField(FIELD_RESCUED_VILLAGER)
    var rescuedVillager: Int by mutableStateOf(rescuedVillager)

    @JsonField(FIELD_CANNOT_ATTACH_PROJECTILES)
    var cannotAttachProjectiles: Int by mutableStateOf(cannotAttachProjectiles)

    constructor(from: JSONObject): this(
        from.getInt(FIELD_DECOR_ACTOR),
        from.getInt(FIELD_RESCUED_VILLAGER),
        from.getInt(FIELD_CANNOT_ATTACH_PROJECTILES)
    )

}

@Stable
class Item private constructor(
    val parent: DungeonsJsonState,
    inventoryIndex: Int?,
    power: Double,
    rarity: String,
    type: String,
    upgraded: Boolean,
    enchantments: List<Enchantment>? = null,
    armorProperties: List<ArmorProperty>? = null,
    netheriteEnchant: Enchantment? = null,
    modified: Boolean? = null,
    timesModified: Int? = null,
    equipmentSlot: String? = null,
    markedNew: Boolean? = null
) {
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

    val internalId = UUID.randomUUID().toString()

    @JsonField(FIELD_INVENTORY_INDEX) @CanBeUndefined
    var inventoryIndex: Int? by mutableStateOf(inventoryIndex)

    @JsonField(FIELD_POWER)
    var power: Double by mutableStateOf(power)

    @JsonField(FIELD_RARITY)
    var rarity: String by mutableStateOf(rarity)

    @JsonField(FIELD_TYPE)
    var type: String by mutableStateOf(type)

    @JsonField(FIELD_UPGRADED)
    var upgraded: Boolean by mutableStateOf(upgraded)

    @JsonField(FIELD_ENCHANTMENTS) @CanBeUndefined
    var enchantments: SnapshotStateList<Enchantment>? by mutableStateOf(
        enchantments
            ?.onEach { it.holder = this }
            ?.toMutableList()
            ?.padEnd(9) { Enchantment(this@Item, "Unset") }
            ?.toMutableStateList()
    )

    @JsonField(FIELD_ARMOR_PROPERTIES) @CanBeUndefined
    var armorProperties: SnapshotStateList<ArmorProperty>? by mutableStateOf(armorProperties?.onEach { it.holder = this }?.toMutableStateList())

    @JsonField(FIELD_NETHERITE_ENCHANT) @CanBeUndefined
    var netheriteEnchant: Enchantment? by mutableStateOf(netheriteEnchant?.also { it.holder = this; it.isNetheriteEnchant = true })

    @JsonField(FIELD_MODIFIED) @CanBeUndefined
    var modified: Boolean? by mutableStateOf(modified)

    @JsonField(FIELD_TIMES_MODIFIED) @CanBeUndefined
    var timesModified: Int? by mutableStateOf(timesModified)

    @JsonField(FIELD_EQUIPMENT_SLOT) @CanBeUndefined
    var equipmentSlot: String? by mutableStateOf(equipmentSlot)

    @JsonField(FIELD_MARKED_NEW) @CanBeUndefined
    var markedNew: Boolean? by mutableStateOf(markedNew)

    val data by derivedStateOf { DungeonsDatabase.item(this.type) ?: throw RuntimeException("unknown item type $type") }

    val totalEnchantmentInvestedPoints by derivedStateOf { this.enchantments?.sumOf { it.investedPoints } ?: 0 }

    val enchanted by derivedStateOf { totalEnchantmentInvestedPoints > 0 }

    val glided by derivedStateOf { netheriteEnchant != null && netheriteEnchant.id != "Unset" }

    val where by derivedStateOf {
        if (parent.items.contains(this)) EditorState.EditorView.Inventory
        else if (parent.storageChestItems.contains(this)) EditorState.EditorView.Storage
        else null
    }

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        parent,
        from.safe { getInt(FIELD_INVENTORY_INDEX) },
        from.getDouble(FIELD_POWER),
        from.getString(FIELD_RARITY),
        from.getString(FIELD_TYPE),
        from.getBoolean(FIELD_UPGRADED),
        from.safe { getJSONArray(FIELD_ENCHANTMENTS).transformWithJsonObject { Enchantment(it) } },
        from.safe { getJSONArray(FIELD_ARMOR_PROPERTIES).transformWithJsonObject { ArmorProperty(it) } },
        from.safe { Enchantment(getJSONObject(FIELD_NETHERITE_ENCHANT)) },
        from.safe { getBoolean(FIELD_MODIFIED) },
        from.safe { getInt(FIELD_TIMES_MODIFIED) },
        from.safe { getString(FIELD_EQUIPMENT_SLOT) },
        from.safe { getBoolean(FIELD_MARKED_NEW) }
    )

    constructor(other: Item): this(
        other.parent,
        other.inventoryIndex,
        other.power,
        other.rarity,
        other.type,
        other.upgraded,
        other.enchantments?.map { it.copy() },
        other.armorProperties?.map { it.copy() },
        other.netheriteEnchant?.copy(),
        other.modified,
        other.timesModified,
        other.equipmentSlot,
        other.markedNew
    )

    fun transfer(editor: EditorState) {
        val previousIndex = inventoryIndex
        val previousWhere = where
        val selectionSlot = editor.selection.slotOf(this)

        if (previousWhere == EditorState.EditorView.Inventory) {
            parent.items.remove(this)
            parent.storageChestItems.add(0, this)
        } else {
            parent.storageChestItems.remove(this)
            parent.items.add(0, this)
        }
        parent.unequippedItems.forEachIndexed { index, item -> item.inventoryIndex = index }
        parent.storageChestItems.forEachIndexed { index, item -> item.inventoryIndex = index }

        if (editor.view == previousWhere) {
            editor.selection.unselect(this)

            val searchFrom =
                if (editor.view == EditorState.EditorView.Inventory) parent.items
                else parent.storageChestItems
            val newSelection = searchFrom.find { it.inventoryIndex == previousIndex }
            if (newSelection != null && selectionSlot != null)
                editor.selection.select(newSelection, selectionSlot, unselectIfAlreadySelected = false)
        }
    }

    fun copy(): Item = Item(this).apply {
        equipmentSlot = null
        inventoryIndex = 0
        markedNew = true
    }

    fun export(): JSONObject =
        JSONObject().apply {
            inventoryIndex?.let { put(FIELD_INVENTORY_INDEX, it) }
            armorProperties?.let { put(FIELD_ARMOR_PROPERTIES, it.map { property -> property.export() }) }
            modified?.let { put(FIELD_MODIFIED, it) }
            timesModified?.let { put(FIELD_TIMES_MODIFIED, it) }
            equipmentSlot?.let { put(FIELD_EQUIPMENT_SLOT, it) }
            markedNew?.let { put(FIELD_MARKED_NEW, it) }
            netheriteEnchant?.let { if (it.id != "Unset") put(FIELD_NETHERITE_ENCHANT, it.export()) }
            enchantments?.let {
                put(
                    FIELD_ENCHANTMENTS,
                    it
                        .chunked(3)
                        .filter { slot -> !slot.all { enchantment -> enchantment.id == "Unset" } }
                        .flatten()
                        .map { enchantment -> enchantment.export() }
                )
            }

            put(FIELD_POWER, power)
            put(FIELD_RARITY, rarity)
            put(FIELD_TYPE, type)
            put(FIELD_UPGRADED, upgraded)
        }

    fun newNetheriteEnchant() =
        Enchantment(this, "Unset", isNetheriteEnchant = true).also { this.netheriteEnchant = it }
}

@Stable
class Enchantment private constructor(
    id: String,
    investedPoints: Int,
    level: Int
) {
    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_INVESTED_POINTS = "investedPoints"
        private const val FIELD_LEVEL = "level"

        fun Unset(holder: Item) = Enchantment(holder, "Unset", 0, 0)
    }

    lateinit var holder: Item
    var isNetheriteEnchant: Boolean = false

    @JsonField(FIELD_ID)
    var id: String by mutableStateOf(id)

    @JsonField(FIELD_INVESTED_POINTS)
    var investedPoints: Int by mutableStateOf(investedPoints)

    @JsonField(FIELD_LEVEL)
    var level: Int by mutableStateOf(level)

    val data by derivedStateOf { DungeonsDatabase.enchantment(this.id) ?: throw RuntimeException("unknown enchantment id ${this.id}") }

    constructor(from: JSONObject): this(
        from.getString(FIELD_ID),
        from.getInt(FIELD_INVESTED_POINTS),
        from.getInt(FIELD_LEVEL)
    )

    constructor(holder: Item, id: String, investedPoints: Int = 0, level: Int = 0, isNetheriteEnchant: Boolean = false):
            this(id, investedPoints, level) { this.holder = holder; this.isNetheriteEnchant = isNetheriteEnchant }

    fun copy(
        holder: Item = this.holder,
        id: String = this.id,
        investedPoints: Int = this.investedPoints,
        level: Int = this.level,
        isNetheriteEnchant: Boolean = this.isNetheriteEnchant,
    ) = Enchantment(holder, id, investedPoints, level, isNetheriteEnchant)

    fun export(): JSONObject =
        JSONObject().apply {
            put(FIELD_ID, id)
            put(FIELD_INVESTED_POINTS, investedPoints)
            put(FIELD_LEVEL, level)
        }

    fun applyInvestedPoints(newLevel: Int = this.level) {
        level = newLevel

        val nonGlided = holder.netheriteEnchant == null || holder.netheriteEnchant?.id == "Unset"
        investedPoints =
            if (isNetheriteEnchant)
                0
            else if (!data.powerful && nonGlided)
                EnchantmentData.CommonNonGlidedInvestedPoints.slice(0 until newLevel).sum()
            else if (data.powerful && nonGlided)
                EnchantmentData.PowerfulNonGlidedInvestedPoints.slice(0 until newLevel).sum()
            else if (!data.powerful && !nonGlided)
                EnchantmentData.CommonGlidedInvestedPoints.slice(0 until newLevel).sum()
            else if (data.powerful && !nonGlided)
                EnchantmentData.PowerfulGlidedInvestedPoints.slice(0 until newLevel).sum()
            else 0
    }

    val isUnset get() = id == "Unset"

}

@Stable
class ArmorProperty private constructor(
    id: String,
    rarity: String
) {
    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_RARITY = "rarity"
    }

    lateinit var holder: Item

    @JsonField(FIELD_ID)
    var id: String by mutableStateOf(id)

    @JsonField(FIELD_RARITY)
    var rarity: String by mutableStateOf(rarity)

    val data by derivedStateOf { DungeonsDatabase.armorProperty(id) ?: throw RuntimeException("unknown armor property id $id") }

    constructor(from: JSONObject): this(
        from.getString(FIELD_ID),
        from.getString(FIELD_RARITY)
    )

    constructor(holder: Item, id: String, rarity: String = "Common") : this(id, rarity) { this.holder = holder }

    fun copy() = ArmorProperty(holder, id, rarity)

    fun export(): JSONObject =
        JSONObject().apply {
            put(FIELD_ID, id)
            put(FIELD_RARITY, rarity)
        }
}

@Stable
class LobbyChestProgress(unlockedTimes: Int) {
    companion object {
        private const val FIELD_UNLOCKED_TIMES = "unlockedTimes"
    }

    @JsonField(FIELD_UNLOCKED_TIMES)
    var unlockedTimes by mutableStateOf(unlockedTimes)

    constructor(from: JSONObject): this(from.getInt(FIELD_UNLOCKED_TIMES))
}

@Stable
class MapUiState(
    panPosition: PanPosition,
    selectedDifficulty: String,
    selectedMission: String,
    selectedRealm: String,
    selectedThreatLevel: String
) {
    companion object {
        private const val FIELD_PAN_POSITION = "panPosition"
        private const val FIELD_SELECTED_DIFFICULTY = "selectedDifficulty"
        private const val FIELD_SELECTED_MISSION = "selectedMission"
        private const val FIELD_SELECTED_REALM = "selectedRealm"
        private const val FIELD_SELECTED_THREAT_LEVEL = "selectedThreadLevel"
    }

    @JsonField(FIELD_PAN_POSITION)
    val panPosition by mutableStateOf(panPosition)

    @JsonField(FIELD_SELECTED_DIFFICULTY)
    var selectedDifficulty by mutableStateOf(selectedDifficulty)

    @JsonField(FIELD_SELECTED_MISSION)
    var selectedMission by mutableStateOf(selectedMission)

    @JsonField(FIELD_SELECTED_REALM)
    var selectedRealm by mutableStateOf(selectedRealm)

    @JsonField(FIELD_SELECTED_THREAT_LEVEL)
    var selectedThreatLevel by mutableStateOf(selectedThreatLevel)

    constructor(from: JSONObject): this(
        PanPosition(from.getJSONObject(FIELD_PAN_POSITION)),
        from.getString(FIELD_SELECTED_DIFFICULTY),
        from.getString(FIELD_SELECTED_MISSION),
        from.getString(FIELD_SELECTED_REALM),
        from.getString(FIELD_SELECTED_THREAT_LEVEL)
    )
}

@Stable
class PanPosition(
    x: String,
    y: String
) {
    companion object {
        private const val FIELD_X = "x"
        private const val FIELD_Y = "y"
    }

    @JsonField(FIELD_X)
    var x: String by mutableStateOf(x)

    @JsonField(FIELD_Y)
    var y: String by mutableStateOf(y)

    constructor(from: JSONObject): this(from.getString(FIELD_X), from.getString(FIELD_Y))
}

@Stable
class MissionStatesHolder(missionStates: List<Any>) {
    companion object {
        private const val FIELD_MISSION_STATES = "missionStates"
    }

    @JsonField(FIELD_MISSION_STATES) @MustBeVerified("type of list is unknown(Any), which must be verified.")
    val missionStates = missionStates.toMutableStateList()

    constructor(from: JSONObject): this(from.getJSONArray(FIELD_MISSION_STATES).transform { it })
}

@Stable
class StageProgress(
    completedDifficulty: String,
    completedEndlessStruggle: Int,
    completedThreatLevel: String
) {
    companion object {
        private const val FIELD_COMPLETED_DIFFICULTY = "completedDifficulty"
        private const val FIELD_COMPLETED_ENDLESS_STRUGGLE = "completedEndlessStruggle"
        private const val FIELD_COMPLETED_THREAT_LEVEL = "completedThreatLevel"
    }

    @JsonField(FIELD_COMPLETED_DIFFICULTY)
    var completedDifficulty by mutableStateOf(completedDifficulty)

    @JsonField(FIELD_COMPLETED_ENDLESS_STRUGGLE)
    var completedEndlessStruggle by mutableStateOf(completedEndlessStruggle)

    @JsonField(FIELD_COMPLETED_THREAT_LEVEL)
    var completedThreatLevel by mutableStateOf(completedThreatLevel)

    constructor(from: JSONObject): this(
        from.getString(FIELD_COMPLETED_DIFFICULTY),
        from.getInt(FIELD_COMPLETED_ENDLESS_STRUGGLE),
        from.getString(FIELD_COMPLETED_THREAT_LEVEL)
    )
}

@Stable
class StorageChestAnnouncement(
    inventoryAnnounced: Boolean,
    storageAnnounced: Boolean
) {
    companion object {
        private const val FIELD_INVENTORY_ANNOUNCED = "inventoryAnnounced"
        private const val FIELD_STORAGE_ANNOUNCED = "storageAnnounced"
    }

    @JsonField(FIELD_INVENTORY_ANNOUNCED)
    var inventoryAnnounced: Boolean by mutableStateOf(inventoryAnnounced)

    @JsonField(FIELD_STORAGE_ANNOUNCED)
    var storageAnnounced: Boolean by mutableStateOf(storageAnnounced)

    constructor(from: JSONObject): this(
        from.getBoolean(FIELD_INVENTORY_ANNOUNCED),
        from.getBoolean(FIELD_STORAGE_ANNOUNCED)
    )
}

@Stable
class ThreatLevels(
    unlocked: String
) {
    companion object {
        private const val FIELD_UNLOCKED = "unlocked"
    }

    @JsonField(FIELD_UNLOCKED)
    var unlocked: String by mutableStateOf(unlocked)

    constructor(from: JSONObject): this(from.getString(FIELD_UNLOCKED))
}

class UiHint(
    hintType: String
) {
    companion object {
        private const val FIELD_HINT_TYPE = "hintType"
    }

    @JsonField(FIELD_HINT_TYPE)
    var hintType: String by mutableStateOf(hintType)

    constructor(from: JSONObject): this(from.getString(FIELD_HINT_TYPE))
}

@Stable
class CompletedTrial(
    id: String,
    difficulty: String
) {
    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_DIFFICULTY = "difficulty"
    }

    @JsonField(FIELD_ID)
    var id: String by mutableStateOf(id)

    @JsonField(FIELD_DIFFICULTY)
    var difficulty: String by mutableStateOf(difficulty)

    constructor(from: JSONObject): this(
        from.getString(FIELD_ID),
        from.getString(FIELD_DIFFICULTY)
    )
}

@Stable
class MerchantDataSet(
    adventureHub: AdventureHubMerchantData?,
    blacksmith: BlacksmithMerchantData?,
    enchantment: EnchantmentMerchantData?,
    giftWrapper: GiftWrapperMerchantData?,
    hyperMission: HyperMissionMerchantData?,
    luxury: LuxuryMerchantData?,
    piglin: PiglinMerchantData?,
    storageChest: StorageChestMerchantData?,
    towerArtisan: TowerArtisanMerchantData?,
    towerBlacksmith: TowerBlacksmithMerchantData?,
    towerComplete: TowerCompleteMerchantData?,
    towerFloorComplete: TowerFloorCompleteMerchantData?,
    towerGlider: TowerGliderMerchantData?,
    villager: VillagerMerchantData?
) {
    companion object {
        private const val FIELD_ADVENTURE_HUB = "Default__AdventureHubMerchantDef"
        private const val FIELD_BLACKSMITH = "Default__BlacksmithMerchantDef"
        private const val FIELD_ENCHANTMENT = "Default__EnchantmentMerchantDef"
        private const val FIELD_GIFT_WRAPPER = "Default__GiftWrapperMerchantDef"
        private const val FIELD_HYPER_MISSION = "Default__HyperMissionMerchantDef"
        private const val FIELD_LUXURY = "Default__LuxuryMerchantDef"
        private const val FIELD_PIGLIN = "Default__PiglinMerchantDef"
        private const val FIELD_STORAGE_CHEST = "Default__StorageChestMerchantDef"
        private const val FIELD_TOWER_ARTISAN = "Default__TowerArtisanMerchantDef"
        private const val FIELD_TOWER_BLACKSMITH = "Default__TowerBlacksmithMerchantDef"
        private const val FIELD_TOWER_COMPLETE = "Default__TowerCompleteMerchantDef"
        private const val FIELD_TOWER_FLOOR_COMPLETE = "Default__TowerFloorCompleteMerchantDef"
        private const val FIELD_TOWER_GLIDER = "Default__TowerGliderMerchantDef"
        private const val FIELD_VILLAGER = "Default__VillagerMerchantDef"
    }

    @JsonField(FIELD_ADVENTURE_HUB) @CanBeUndefined
    var adventureHub: AdventureHubMerchantData? by mutableStateOf(adventureHub)

    @JsonField(FIELD_BLACKSMITH) @CanBeUndefined
    var blacksmith: BlacksmithMerchantData? by mutableStateOf(blacksmith)

    @JsonField(FIELD_ENCHANTMENT) @CanBeUndefined
    var enchantment: EnchantmentMerchantData? by mutableStateOf(enchantment)

    @JsonField(FIELD_GIFT_WRAPPER) @CanBeUndefined
    var giftWrapper: GiftWrapperMerchantData? by mutableStateOf(giftWrapper)

    @JsonField(FIELD_HYPER_MISSION) @CanBeUndefined
    var hyperMission: HyperMissionMerchantData? by mutableStateOf(hyperMission)

    @JsonField(FIELD_LUXURY) @CanBeUndefined
    var luxury: LuxuryMerchantData? by mutableStateOf(luxury)

    @JsonField(FIELD_PIGLIN) @CanBeUndefined
    var piglin: PiglinMerchantData? by mutableStateOf(piglin)

    @JsonField(FIELD_STORAGE_CHEST) @CanBeUndefined
    var storageChest: StorageChestMerchantData? by mutableStateOf(storageChest)

    @JsonField(FIELD_TOWER_ARTISAN) @CanBeUndefined
    var towerArtisan: TowerArtisanMerchantData? by mutableStateOf(towerArtisan)

    @JsonField(FIELD_TOWER_BLACKSMITH) @CanBeUndefined
    var towerBlacksmith: TowerBlacksmithMerchantData? by mutableStateOf(towerBlacksmith)

    @JsonField(FIELD_TOWER_COMPLETE) @CanBeUndefined
    var towerComplete: TowerCompleteMerchantData? by mutableStateOf(towerComplete)

    @JsonField(FIELD_TOWER_FLOOR_COMPLETE) @CanBeUndefined
    var towerFloorComplete: TowerFloorCompleteMerchantData? by mutableStateOf(towerFloorComplete)

    @JsonField(FIELD_TOWER_GLIDER) @CanBeUndefined
    var towerGlider: TowerGliderMerchantData? by mutableStateOf(towerGlider)

    @JsonField(FIELD_VILLAGER) @CanBeUndefined
    var villager: VillagerMerchantData? by mutableStateOf(villager)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.safe { AdventureHubMerchantData(getJSONObject(FIELD_ADVENTURE_HUB)) },
        from.safe { BlacksmithMerchantData(parent, getJSONObject(FIELD_BLACKSMITH)) },
        from.safe { EnchantmentMerchantData(getJSONObject(FIELD_ENCHANTMENT)) },
        from.safe { GiftWrapperMerchantData(parent, getJSONObject(FIELD_GIFT_WRAPPER)) },
        from.safe { HyperMissionMerchantData(parent, getJSONObject(FIELD_HYPER_MISSION)) },
        from.safe { LuxuryMerchantData(parent, getJSONObject(FIELD_LUXURY)) },
        from.safe { PiglinMerchantData(parent, getJSONObject(FIELD_PIGLIN)) },
        from.safe { StorageChestMerchantData(getJSONObject(FIELD_STORAGE_CHEST)) },
        from.safe { TowerArtisanMerchantData(parent, getJSONObject(FIELD_TOWER_ARTISAN)) },
        from.safe { TowerBlacksmithMerchantData(getJSONObject(FIELD_TOWER_BLACKSMITH)) },
        from.safe { TowerCompleteMerchantData(parent, getJSONObject(FIELD_TOWER_COMPLETE)) },
        from.safe { TowerFloorCompleteMerchantData(getJSONObject(FIELD_TOWER_FLOOR_COMPLETE)) },
        from.safe { TowerGliderMerchantData(parent, getJSONObject(FIELD_TOWER_GLIDER)) },
        from.safe { VillagerMerchantData(parent, getJSONObject(FIELD_VILLAGER)) }
    )
}

@Stable
abstract class MerchantData<Quests, Slots>(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: Quests,
    slots: Slots
) {
    companion object {
        const val FIELD_EVER_INTERACTED = "everInteracted"
        const val FIELD_PRICING = "pricing"
        const val FIELD_QUESTS = "quests"
        const val FIELD_SLOTS = "slots"
    }

    @JsonField(FIELD_EVER_INTERACTED)
    var everInteracted: Boolean by mutableStateOf(everInteracted)

    @JsonField(FIELD_PRICING)
    val pricing: MerchantPricing by mutableStateOf(pricing)

    @JsonField(FIELD_QUESTS)
    val quests: Quests by mutableStateOf(quests)

    @JsonField(FIELD_SLOTS)
    val slots: Slots by mutableStateOf(slots)
}

@Stable
class EmptyMerchantDataQuests

@Stable
class EmptyMerchantDataSlots

@Stable
class AdventureHubMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: EmptyMerchantDataQuests = EmptyMerchantDataQuests(),
    slots: EmptyMerchantDataSlots = EmptyMerchantDataSlots()
): MerchantData<EmptyMerchantDataQuests, EmptyMerchantDataSlots>(everInteracted, pricing, quests, slots) {
    constructor(from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING))
    )
}

@Stable
class StorageChestMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: EmptyMerchantDataQuests = EmptyMerchantDataQuests(),
    slots: EmptyMerchantDataSlots = EmptyMerchantDataSlots()
): MerchantData<EmptyMerchantDataQuests, EmptyMerchantDataSlots>(everInteracted, pricing, quests, slots) {
    constructor(from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING))
    )
}

@Stable
class TowerBlacksmithMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: EmptyMerchantDataQuests = EmptyMerchantDataQuests(),
    slots: EmptyMerchantDataSlots = EmptyMerchantDataSlots()
): MerchantData<EmptyMerchantDataQuests, EmptyMerchantDataSlots>(everInteracted, pricing, quests, slots) {
    constructor(from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING))
    )
}

@Stable
class TowerFloorCompleteMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: EmptyMerchantDataQuests = EmptyMerchantDataQuests(),
    slots: EmptyMerchantDataSlots = EmptyMerchantDataSlots()
): MerchantData<EmptyMerchantDataQuests, EmptyMerchantDataSlots>(everInteracted, pricing, quests, slots) {
    constructor(from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING))
    )
}

@Stable
class BlacksmithMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: BlacksmithQuests,
    slots: BlacksmithSlots
): MerchantData<BlacksmithQuests, BlacksmithSlots>(everInteracted, pricing, quests, slots) {
    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING)),
        BlacksmithQuests(from.getJSONObject(FIELD_QUESTS)),
        BlacksmithSlots(parent, from.getJSONObject(FIELD_SLOTS)),
    )
}

@Stable
class BlacksmithQuests(
    quest1: MerchantQuestStateHolder?,
    quest2: MerchantQuestStateHolder?,
    upgradeQuest1: BlacksmithUpgradeQuest,
    upgradeQuest2: BlacksmithUpgradeQuest,
    upgradeQuest3: BlacksmithUpgradeQuest
) {
    companion object {
        private const val FIELD_QUEST_1 = "Quest1"
        private const val FIELD_QUEST_2 = "Quest2"
        private const val FIELD_UPGRADE_QUEST_1 = "UpgradeQuest1"
        private const val FIELD_UPGRADE_QUEST_2 = "UpgradeQuest2"
        private const val FIELD_UPGRADE_QUEST_3 = "UpgradeQuest3"
    }

    @JsonField(FIELD_QUEST_1) @MustBeVerified("object type is unknown(Nothing), which must be verified")
    val quest1: MerchantQuestStateHolder? by mutableStateOf(quest1)

    @JsonField(FIELD_QUEST_2) @MustBeVerified("object type is unknown(Nothing), which must be verified")
    val quest2: MerchantQuestStateHolder? by mutableStateOf(quest2)

    @JsonField(FIELD_UPGRADE_QUEST_1) @MustBeVerified("Is this can be undefined or null?")
    val upgradeQuest1: BlacksmithUpgradeQuest by mutableStateOf(upgradeQuest1)

    @JsonField(FIELD_UPGRADE_QUEST_2) @MustBeVerified("Is this can be undefined or null?")
    val upgradeQuest2: BlacksmithUpgradeQuest by mutableStateOf(upgradeQuest2)

    @JsonField(FIELD_UPGRADE_QUEST_3) @MustBeVerified("Is this can be undefined or null?")
    val upgradeQuest3: BlacksmithUpgradeQuest by mutableStateOf(upgradeQuest3)

    constructor(from: JSONObject): this(
        from.safe { MerchantQuestStateHolder(getJSONObject(FIELD_QUEST_1)) },
        from.safe { MerchantQuestStateHolder(getJSONObject(FIELD_QUEST_2)) },
        BlacksmithUpgradeQuest(from.getJSONObject(FIELD_UPGRADE_QUEST_1)),
        BlacksmithUpgradeQuest(from.getJSONObject(FIELD_UPGRADE_QUEST_2)),
        BlacksmithUpgradeQuest(from.getJSONObject(FIELD_UPGRADE_QUEST_3))
    )
}

@Stable
class BlacksmithUpgradeQuest(
    dynamicQuestState: BlacksmithDynamicQuestState,
    questState: MerchantQuestState
) {
    companion object {
        private const val FIELD_DYNAMIC_QUEST_STATE = "dynamicQuestState"
        private const val FIELD_QUEST_STATE = "questState"
    }

    @JsonField(FIELD_DYNAMIC_QUEST_STATE)
    val dynamicQuestState: BlacksmithDynamicQuestState by mutableStateOf(dynamicQuestState)

    @JsonField(FIELD_QUEST_STATE)
    val questState: MerchantQuestState by mutableStateOf(questState)

    constructor(from: JSONObject): this(
        BlacksmithDynamicQuestState(from.getJSONObject(FIELD_DYNAMIC_QUEST_STATE)),
        MerchantQuestState(from.getJSONObject(FIELD_QUEST_STATE))
    )
}

@Stable
class BlacksmithDynamicQuestState(
    selectedProgressState: String
) {
    companion object {
        private const val FIELD_SELECTED_PROGRESS_STATE = "selectedProgressStat"
    }

    @JsonField(FIELD_SELECTED_PROGRESS_STATE)
    var selectedProgressState: String by mutableStateOf(selectedProgressState)

    constructor(from: JSONObject): this(from.getString(FIELD_SELECTED_PROGRESS_STATE))
}

@Stable
class BlacksmithSlots(
    slot1: MerchantDataSlot,
    slot2: MerchantDataSlot,
    slot3: MerchantDataSlot
) {
    companion object {
        private const val FIELD_SLOT_1 = "Slot1"
        private const val FIELD_SLOT_2 = "Slot2"
        private const val FIELD_SLOT_3 = "Slot3"
    }

    @JsonField(FIELD_SLOT_1)
    val slot1: MerchantDataSlot by mutableStateOf(slot1)

    @JsonField(FIELD_SLOT_2)
    val slot2: MerchantDataSlot by mutableStateOf(slot2)

    @JsonField(FIELD_SLOT_3)
    val slot3: MerchantDataSlot by mutableStateOf(slot3)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_1)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_2)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_3))
    )
}

@Stable
class EnchantmentMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: EmptyMerchantDataQuests = EmptyMerchantDataQuests(),
    slots: EmptyMerchantDataSlots = EmptyMerchantDataSlots()
): MerchantData<EmptyMerchantDataQuests, EmptyMerchantDataSlots>(everInteracted, pricing, quests, slots) {
    constructor(from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING))
    )
}

@Stable
class GiftWrapperMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: GiftWrapperQuests,
    slots: GiftWrapperSlots
): MerchantData<GiftWrapperQuests, GiftWrapperSlots>(everInteracted, pricing, quests, slots) {
    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING)),
        GiftWrapperQuests(from),
        GiftWrapperSlots(parent, from)
    )
}

@Stable
class GiftWrapperQuests(
    restockQuest: MerchantQuestStateHolder
) {
    companion object {
        const val FIELD_RESTOCK_QUEST = "RestockQuest"
    }

    @JsonField(FIELD_RESTOCK_QUEST)
    val restockQuest: MerchantQuestStateHolder by mutableStateOf(restockQuest)

    constructor(from: JSONObject): this(MerchantQuestStateHolder(from.getJSONObject(FIELD_RESTOCK_QUEST)))
}

@Stable
class GiftWrapperSlots(
    slot1: MerchantDataSlot,
) {
    companion object {
        private const val FIELD_SLOT_1 = "Slot1"
    }

    @JsonField(FIELD_SLOT_1)
    val slot1: MerchantDataSlot by mutableStateOf(slot1)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_1))
    )
}

@Stable
class HyperMissionMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: EmptyMerchantDataQuests = EmptyMerchantDataQuests(),
    slots: HyperMissionSlots
): MerchantData<EmptyMerchantDataQuests, HyperMissionSlots>(everInteracted, pricing, quests, slots) {
    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING)),
        slots = HyperMissionSlots(parent, from.getJSONObject(FIELD_SLOTS))
    )
}

@Stable
class HyperMissionSlots(
    slotArmor: MerchantDataSlot,
    slotArtifact: MerchantDataSlot,
    slotMelee: MerchantDataSlot,
    slotRanged: MerchantDataSlot
) {
    companion object {
        private const val FIELD_SLOT_ARMOR = "SlotArmor"
        private const val FIELD_SLOT_ARTIFACT = "SlotArtifact"
        private const val FIELD_SLOT_MELEE = "SlotMelee"
        private const val FIELD_SLOT_RANGED = "SlotRanged"
    }

    @JsonField(FIELD_SLOT_ARMOR)
    val slotArmor: MerchantDataSlot by mutableStateOf(slotArmor)

    @JsonField(FIELD_SLOT_ARTIFACT)
    val slotArtifact: MerchantDataSlot by mutableStateOf(slotArtifact)

    @JsonField(FIELD_SLOT_MELEE)
    val slotMelee: MerchantDataSlot by mutableStateOf(slotMelee)

    @JsonField(FIELD_SLOT_RANGED)
    val slotRanged: MerchantDataSlot by mutableStateOf(slotRanged)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_ARMOR)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_ARTIFACT)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_MELEE)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_RANGED))
    )
}

@Stable
class LuxuryMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: LuxuryQuests,
    slots: LuxurySlots
): MerchantData<LuxuryQuests, LuxurySlots>(everInteracted, pricing, quests, slots) {
    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING)),
        LuxuryQuests(from.getJSONObject(FIELD_QUESTS)),
        LuxurySlots(parent, from.getJSONObject(FIELD_SLOTS))
    )
}

@Stable
class LuxuryQuests(
    pricingQuest: MerchantQuestStateHolder,
    quest1: MerchantQuestStateHolder,
    quest2: MerchantQuestStateHolder,
    restockQuest: MerchantQuestStateHolder
) {
    companion object {
        private const val FIELD_PRICING_QUEST = "PricingQuest"
        private const val FIELD_QUEST_1 = "Quest1"
        private const val FIELD_QUEST_2 = "Quest2"
        private const val FIELD_RESTOCK_QUEST = "RestockQuest"
    }

    @JsonField(FIELD_PRICING_QUEST)
    val pricingQuest: MerchantQuestStateHolder by mutableStateOf(pricingQuest)

    @JsonField(FIELD_QUEST_1)
    val quest1: MerchantQuestStateHolder by mutableStateOf(quest1)

    @JsonField(FIELD_QUEST_2)
    val quest2: MerchantQuestStateHolder by mutableStateOf(quest2)

    @JsonField(FIELD_RESTOCK_QUEST)
    val restockQuest: MerchantQuestStateHolder by mutableStateOf(restockQuest)

    constructor(from: JSONObject): this(
        MerchantQuestStateHolder(from.getJSONObject(FIELD_PRICING_QUEST)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_QUEST_1)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_QUEST_2)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_RESTOCK_QUEST))
    )
}

@Stable
class PiglinMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: PiglinQuests,
    slots: PiglinSlots
): MerchantData<PiglinQuests, PiglinSlots>(everInteracted, pricing, quests, slots) {
    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING)),
        PiglinQuests(from),
        PiglinSlots(parent, from)
    )
}

@Stable
class PiglinQuests(
    pricingQuest: MerchantQuestStateHolder,
    quest1: MerchantQuestStateHolder,
    quest2: MerchantQuestStateHolder,
    restockQuest: MerchantQuestStateHolder
) {
    companion object {
        private const val FIELD_PRICING_QUEST = "PricingQuest"
        private const val FIELD_QUEST_1 = "Quest1"
        private const val FIELD_QUEST_2 = "Quest2"
        private const val FIELD_RESTOCK_QUEST = "RestockQuest"
    }

    @JsonField(FIELD_PRICING_QUEST)
    val pricingQuest: MerchantQuestStateHolder by mutableStateOf(pricingQuest)

    @JsonField(FIELD_QUEST_1)
    val quest1: MerchantQuestStateHolder by mutableStateOf(quest1)

    @JsonField(FIELD_QUEST_2)
    val quest2: MerchantQuestStateHolder by mutableStateOf(quest2)

    @JsonField(FIELD_RESTOCK_QUEST)
    val restockQuest: MerchantQuestStateHolder by mutableStateOf(restockQuest)

    constructor(from: JSONObject): this(
        MerchantQuestStateHolder(from.getJSONObject(FIELD_PRICING_QUEST)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_QUEST_1)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_QUEST_2)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_RESTOCK_QUEST))
    )
}

@Stable
class PiglinSlots(
    slot1: MerchantDataSlot,
    slot2: MerchantDataSlot,
    slot3: MerchantDataSlot
) {
    companion object {
        private const val FIELD_SLOT_1 = "Slot1"
        private const val FIELD_SLOT_2 = "Slot2"
        private const val FIELD_SLOT_3 = "Slot3"
    }

    @JsonField(FIELD_SLOT_1)
    val slot1: MerchantDataSlot by mutableStateOf(slot1)

    @JsonField(FIELD_SLOT_2)
    val slot2: MerchantDataSlot by mutableStateOf(slot2)

    @JsonField(FIELD_SLOT_3)
    val slot3: MerchantDataSlot by mutableStateOf(slot3)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_1)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_2)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_3))
    )
}

@Stable
class LuxurySlots(
    slot1: MerchantDataSlot,
    slot2: MerchantDataSlot,
    slot3: MerchantDataSlot
) {
    companion object {
        private const val FIELD_SLOT_1 = "Slot1"
        private const val FIELD_SLOT_2 = "Slot2"
        private const val FIELD_SLOT_3 = "Slot3"
    }

    @JsonField(FIELD_SLOT_1)
    val slot1: MerchantDataSlot by mutableStateOf(slot1)

    @JsonField(FIELD_SLOT_2)
    val slot2: MerchantDataSlot by mutableStateOf(slot2)

    @JsonField(FIELD_SLOT_3)
    val slot3: MerchantDataSlot by mutableStateOf(slot3)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_1)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_2)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_3))
    )
}

@Stable
class TowerArtisanMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: EmptyMerchantDataQuests = EmptyMerchantDataQuests(),
    slots: TowerArtisanSlots
): MerchantData<EmptyMerchantDataQuests, TowerArtisanSlots>(everInteracted, pricing, quests, slots) {
    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING)),
        slots = TowerArtisanSlots(parent, from.getJSONObject(FIELD_SLOTS))
    )
}

@Stable
class TowerArtisanSlots(
    slotArmor: MerchantDataSlot,
    slotArtifact1: MerchantDataSlot,
    slotArtifact2: MerchantDataSlot,
    slotArtifact3: MerchantDataSlot,
    slotMelee: MerchantDataSlot,
    slotRanged: MerchantDataSlot
) {
    companion object {
        private const val FIELD_SLOT_ARMOR = "SlotArmor"
        private const val FIELD_SLOT_ARTIFACT_1 = "SlotArtifact1"
        private const val FIELD_SLOT_ARTIFACT_2 = "SlotArtifact2"
        private const val FIELD_SLOT_ARTIFACT_3 = "SlotArtifact3"
        private const val FIELD_SLOT_MELEE = "SlotMelee"
        private const val FIELD_SLOT_RANGED = "SlotRanged"
    }

    @JsonField(FIELD_SLOT_ARMOR)
    val slotArmor: MerchantDataSlot by mutableStateOf(slotArmor)

    @JsonField(FIELD_SLOT_ARTIFACT_1)
    val slotArtifact1: MerchantDataSlot by mutableStateOf(slotArtifact1)

    @JsonField(FIELD_SLOT_ARTIFACT_2)
    val slotArtifact2: MerchantDataSlot by mutableStateOf(slotArtifact2)

    @JsonField(FIELD_SLOT_ARTIFACT_3)
    val slotArtifact3: MerchantDataSlot by mutableStateOf(slotArtifact3)

    @JsonField(FIELD_SLOT_MELEE)
    val slotMelee: MerchantDataSlot by mutableStateOf(slotMelee)

    @JsonField(FIELD_SLOT_RANGED)
    val slotRanged: MerchantDataSlot by mutableStateOf(slotRanged)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_ARMOR)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_ARTIFACT_1)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_ARTIFACT_2)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_ARTIFACT_3)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_MELEE)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_RANGED))
    )
}

@Stable
class TowerCompleteMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: TowerCompleteQuests,
    slots: TowerCompleteSlots
): MerchantData<TowerCompleteQuests, TowerCompleteSlots>(everInteracted, pricing, quests, slots) {
    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING)),
        TowerCompleteQuests(from.getJSONObject(FIELD_QUESTS)),
        TowerCompleteSlots(parent, from.getJSONObject(FIELD_SLOTS))
    )
}

@Stable
class TowerCompleteQuests(
    restockQuest: MerchantQuestStateHolder
) {
    companion object {
        const val FIELD_RESTOCK_QUEST = "RestockQuest"
    }

    @JsonField(FIELD_RESTOCK_QUEST)
    val restockQuest: MerchantQuestStateHolder by mutableStateOf(restockQuest)

    constructor(from: JSONObject): this(MerchantQuestStateHolder(from.getJSONObject(FIELD_RESTOCK_QUEST)))
}

@Stable
class TowerCompleteSlots(
    slot1: MerchantDataSlot,
    slot2: MerchantDataSlot,
    slot3: MerchantDataSlot
) {
    companion object {
        private const val FIELD_SLOT_1 = "Slot1"
        private const val FIELD_SLOT_2 = "Slot2"
        private const val FIELD_SLOT_3 = "Slot3"
    }

    @JsonField(FIELD_SLOT_1)
    val slot1: MerchantDataSlot by mutableStateOf(slot1)

    @JsonField(FIELD_SLOT_2)
    val slot2: MerchantDataSlot by mutableStateOf(slot2)

    @JsonField(FIELD_SLOT_3)
    val slot3: MerchantDataSlot by mutableStateOf(slot3)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_1)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_2)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_3))
    )
}

@Stable
class TowerGliderMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: EmptyMerchantDataQuests = EmptyMerchantDataQuests(),
    slots: TowerGliderSlots
): MerchantData<EmptyMerchantDataQuests, TowerGliderSlots>(everInteracted, pricing, quests, slots) {
    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING)),
        slots = TowerGliderSlots(parent, from.getJSONObject(FIELD_SLOTS))
    )
}

@Stable
class TowerGliderSlots(
    slotArmor: MerchantDataSlot,
    slotArtifact1: MerchantDataSlot,
    slotArtifact2: MerchantDataSlot,
    slotArtifact3: MerchantDataSlot,
    slotMelee: MerchantDataSlot,
    slotRanged: MerchantDataSlot
) {
    companion object {
        private const val FIELD_SLOT_ARMOR = "SlotArmor"
        private const val FIELD_SLOT_ARTIFACT_1 = "SlotArtifact1"
        private const val FIELD_SLOT_ARTIFACT_2 = "SlotArtifact2"
        private const val FIELD_SLOT_ARTIFACT_3 = "SlotArtifact3"
        private const val FIELD_SLOT_MELEE = "SlotMelee"
        private const val FIELD_SLOT_RANGED = "SlotRanged"
    }

    @JsonField(FIELD_SLOT_ARMOR)
    val slotArmor: MerchantDataSlot by mutableStateOf(slotArmor)

    @JsonField(FIELD_SLOT_ARTIFACT_1)
    val slotArtifact1: MerchantDataSlot by mutableStateOf(slotArtifact1)

    @JsonField(FIELD_SLOT_ARTIFACT_2)
    val slotArtifact2: MerchantDataSlot by mutableStateOf(slotArtifact2)

    @JsonField(FIELD_SLOT_ARTIFACT_3)
    val slotArtifact3: MerchantDataSlot by mutableStateOf(slotArtifact3)

    @JsonField(FIELD_SLOT_MELEE)
    val slotMelee: MerchantDataSlot by mutableStateOf(slotMelee)

    @JsonField(FIELD_SLOT_RANGED)
    val slotRanged: MerchantDataSlot by mutableStateOf(slotRanged)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_ARMOR)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_ARTIFACT_1)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_ARTIFACT_2)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_ARTIFACT_3)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_MELEE)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_RANGED))
    )
}

@Stable
class VillagerMerchantData(
    everInteracted: Boolean,
    pricing: MerchantPricing,
    quests: VillagerQuests,
    slots: VillagerSlots
): MerchantData<VillagerQuests, VillagerSlots>(everInteracted, pricing, quests, slots) {
    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.getBoolean(FIELD_EVER_INTERACTED),
        MerchantPricing(from.getJSONObject(FIELD_PRICING)),
        VillagerQuests(from.getJSONObject(FIELD_QUESTS)),
        VillagerSlots(parent, from.getJSONObject(FIELD_SLOTS))
    )
}

@Stable
class VillagerQuests(
    pricingQuest: MerchantQuestStateHolder,
    quest1: MerchantQuestStateHolder,
    quest2: MerchantQuestStateHolder,
    quest3: MerchantQuestStateHolder,
    quest4: MerchantQuestStateHolder,
    restockQuest: MerchantQuestStateHolder
) {
    companion object {
        private const val FIELD_PRICING_QUEST = "PricingQuest"
        private const val FIELD_QUEST_1 = "Quest1"
        private const val FIELD_QUEST_2 = "Quest2"
        private const val FIELD_QUEST_3 = "Quest3"
        private const val FIELD_QUEST_4 = "Quest4"
        private const val FIELD_RESTOCK_QUEST = "RestockQuest"
    }

    @JsonField(FIELD_PRICING_QUEST)
    val pricingQuest: MerchantQuestStateHolder by mutableStateOf(pricingQuest)

    @JsonField(FIELD_QUEST_1)
    val quest1: MerchantQuestStateHolder by mutableStateOf(quest1)

    @JsonField(FIELD_QUEST_2)
    val quest2: MerchantQuestStateHolder by mutableStateOf(quest2)

    @JsonField(FIELD_QUEST_3)
    val quest3: MerchantQuestStateHolder by mutableStateOf(quest3)

    @JsonField(FIELD_QUEST_4)
    val quest4: MerchantQuestStateHolder by mutableStateOf(quest4)

    @JsonField(FIELD_RESTOCK_QUEST)
    val restockQuest: MerchantQuestStateHolder by mutableStateOf(restockQuest)

    constructor(from: JSONObject): this(
        MerchantQuestStateHolder(from.getJSONObject(FIELD_PRICING_QUEST)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_QUEST_1)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_QUEST_2)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_QUEST_3)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_QUEST_4)),
        MerchantQuestStateHolder(from.getJSONObject(FIELD_RESTOCK_QUEST))
    )
}

@Stable
class VillagerSlots(
    slot1: MerchantDataSlot,
    slot2: MerchantDataSlot,
    slot3: MerchantDataSlot,
    slot4: MerchantDataSlot,
    slot5: MerchantDataSlot,
    slot6: MerchantDataSlot,
) {
    companion object {
        private const val FIELD_SLOT_1 = "Slot1"
        private const val FIELD_SLOT_2 = "Slot2"
        private const val FIELD_SLOT_3 = "Slot3"
        private const val FIELD_SLOT_4 = "Slot4"
        private const val FIELD_SLOT_5 = "Slot5"
        private const val FIELD_SLOT_6 = "Slot6"
    }

    @JsonField(FIELD_SLOT_1)
    val slot1: MerchantDataSlot by mutableStateOf(slot1)

    @JsonField(FIELD_SLOT_2)
    val slot2: MerchantDataSlot by mutableStateOf(slot2)

    @JsonField(FIELD_SLOT_3)
    val slot3: MerchantDataSlot by mutableStateOf(slot3)

    @JsonField(FIELD_SLOT_4)
    val slot4: MerchantDataSlot by mutableStateOf(slot4)

    @JsonField(FIELD_SLOT_5)
    val slot5: MerchantDataSlot by mutableStateOf(slot5)

    @JsonField(FIELD_SLOT_6)
    val slot6: MerchantDataSlot by mutableStateOf(slot6)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_1)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_2)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_3)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_4)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_5)),
        MerchantDataSlot(parent, from.getJSONObject(FIELD_SLOT_6))
    )
}


@Stable
class MerchantQuestStateHolder(
    questState: MerchantQuestState
) {
    companion object {
        const val FIELD_QUEST_STATE = "questState"
    }

    @JsonField(FIELD_QUEST_STATE)
    val questState: MerchantQuestState by mutableStateOf(questState)

    constructor(from: JSONObject): this(MerchantQuestState(from.getJSONObject(FIELD_QUEST_STATE)))
}

@Stable
class MerchantQuestState(
    startedAtCount: Int,
    targetCount: Int
) {
    companion object {
        private const val FIELD_STARTED_AT_COUNT = "startedAtCount"
        private const val FIELD_TARGET_COUNT = "targetCount"
    }

    @JsonField(FIELD_STARTED_AT_COUNT)
    var startedAtCount: Int by mutableStateOf(startedAtCount)

    @JsonField(FIELD_TARGET_COUNT)
    var targetCount: Int by mutableStateOf(targetCount)

    constructor(from: JSONObject): this(from.getInt(FIELD_STARTED_AT_COUNT), from.getInt(FIELD_TARGET_COUNT))
}

@Stable
class MerchantDataSlot(
    item: Item?,
    priceMultiplier: Int,
    rebateFraction: Int,
    reserved: Boolean
) {
    companion object {
        private const val FIELD_ITEM = "item"
        private const val FIELD_PRICE_MULTIPLIER = "priceMulitplier"
        private const val FIELD_REBATE_FRACTION = "rebateFraction"
        private const val FIELD_RESERVED = "reserved"
    }

    @JsonField(FIELD_ITEM) @CanBeUndefined
    var item: Item? by mutableStateOf(item)

    @JsonField(FIELD_PRICE_MULTIPLIER) @MustBeVerified("Is type of this property Int?")
    var priceMultiplier: Int by mutableStateOf(priceMultiplier)

    @JsonField(FIELD_REBATE_FRACTION) @MustBeVerified("Is type of this property Int?")
    var rebateFraction: Int by mutableStateOf(rebateFraction)

    @JsonField(FIELD_RESERVED)
    var reserved: Boolean by mutableStateOf(reserved)

    constructor(parent: DungeonsJsonState, from: JSONObject): this(
        from.safe { Item(parent, getJSONObject(FIELD_ITEM)) },
        from.getInt(FIELD_PRICE_MULTIPLIER),
        from.getInt(FIELD_REBATE_FRACTION),
        from.getBoolean(FIELD_RESERVED)
    )
}

@Stable
class MerchantPricing(
    timesRestocked: Int
) {
    companion object {
        private const val FIELD_TIMES_RESTOCKED = "timesRestocked"
    }

    @JsonField(FIELD_TIMES_RESTOCKED)
    var timesRestocked by mutableStateOf(timesRestocked)

    constructor(from: JSONObject): this(from.getInt(FIELD_TIMES_RESTOCKED))
}
