package minecraft.dungeons.values

object DungeonsItem {

    sealed interface Filterable
    sealed interface IconFilterable

    enum class Attributes: Filterable, IconFilterable {
        Enchanted
    }

    enum class Variant: Filterable, IconFilterable {
        Melee, Ranged, Armor, Artifact
    }

    enum class Rarity: Filterable {
        Unique, Rare, Common;

        companion object {
            fun fromSerialized(serialized: String): Rarity = Rarity.valueOf(serialized)
        }
    }

    enum class Location {
        Inventory, Storage;
        fun other() = if (this == Inventory) Storage else Inventory
    }

    enum class EquipmentSlot {
        Melee, Ranged, Armor, HotBar1, HotBar2, HotBar3;

        companion object {
            fun fromSerialized(serialized: String?): EquipmentSlot? =
                when(serialized) {
                    "MeleeGear" -> Melee
                    "RangedGear" -> Ranged
                    "ArmorGear" -> Armor
                    "HotbarSlot1" -> HotBar1
                    "HotbarSlot2" -> HotBar2
                    "HotbarSlot3" -> HotBar3
                    else -> null
                }
        }
    }

}