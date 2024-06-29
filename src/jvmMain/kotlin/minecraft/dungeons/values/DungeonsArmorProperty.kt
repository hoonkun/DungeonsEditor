package minecraft.dungeons.values

object DungeonsArmorProperty {
    enum class Rarity {
        Common, Unique;

        companion object {
            fun fromSerialized(serialized: String) = Rarity.valueOf(serialized)
        }
    }
}