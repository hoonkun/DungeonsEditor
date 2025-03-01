package minecraft.dungeons.values

object DungeonsArmorProperty {

    enum class Rarity(
        val serialized: String,
        texture: String,
    ) {
        Common("Common", "regular"),
        Unique("Unique", "unique");

        val texture = "/UI/Materials/Inventory2/Inspector/${texture}_bullit.png"

        companion object {
            fun fromSerialized(value: String) =
                Rarity.entries.first { it.serialized == value }
        }
    }

}