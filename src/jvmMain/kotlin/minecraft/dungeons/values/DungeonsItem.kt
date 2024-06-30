package minecraft.dungeons.values

import kiwi.hoonkun.resources.Localizations

object DungeonsItem {

    enum class Rarity(val serialized: String): Filterable {
        Unique("Unique"),
        Rare("Rare"),
        Common("Common");

        companion object {
            fun fromSerialized(value: String): Rarity =
                Rarity.entries.first { it.serialized == value }
        }
    }

    enum class EquipmentSlot(val serialized: String) {
        Melee("MeleeGear"),
        Ranged("RangedGear"),
        Armor("ArmorGear"),
        HotBar1("HotbarSlot1"),
        HotBar2("HotbarSlot2"),
        HotBar3("HotbarSlot3");

        companion object {
            fun fromSerialized(value: String?): EquipmentSlot? =
                value?.let { v -> EquipmentSlot.entries.first { it.serialized == v } }
        }
    }

    enum class Attributes: Filterable, IconFilterable {
        Enchanted
    }

    enum class Variant: Filterable, IconFilterable {
        Melee, Ranged, Armor, Artifact
    }

    enum class Location {
        Inventory, Storage;

        fun isInventory() = this == Inventory
        fun isStorage() = this == Storage

        fun other() =
            when (this) {
                Inventory -> Storage
                Storage -> Inventory
            }

        val localizedName get() =
            when (this) {
                Inventory -> Localizations["inventory"]
                Storage -> Localizations["storage"]
            }
    }

    sealed interface Filterable
    sealed interface IconFilterable

}