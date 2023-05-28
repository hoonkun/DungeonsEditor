package blackstone.states.items

import Database
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import arctic
import blackstone.states.Item
import blackstone.states.StoredDataState
import extensions.GameResources


val equippedMelee: (Item) -> Boolean = { it.equipmentSlot == "MeleeGear" }
val equippedRanged: (Item) -> Boolean = { it.equipmentSlot == "RangedGear" }
val equippedArmor: (Item) -> Boolean = { it.equipmentSlot == "ArmorGear" }

val equippedArtifact1: (Item) -> Boolean = { it.equipmentSlot == "HotbarSlot1" }
val equippedArtifact2: (Item) -> Boolean = { it.equipmentSlot == "HotbarSlot2" }
val equippedArtifact3: (Item) -> Boolean = { it.equipmentSlot == "HotbarSlot3" }

val equipped: (Item) -> Boolean = { it.equipmentSlot != null }
val unequipped: (Item) -> Boolean = { it.inventoryIndex != null }

val Item.data get() = Database.current.findItem(type) ?: throw RuntimeException("Unknown item $type")

val Item.totalInvestedEnchantmentPoints get() = enchantments?.sumOf { it.investedPoints } ?: 0

fun Item.recalculateEnchantmentPoints() {
    enchantments?.forEach { it.leveling(it.level) }
}

fun StoredDataState.addItem(newItem: Item, selected: Item? = null) {
    items.add(6, newItem)
    items.filter(unequipped).forEachIndexed { index, item -> item.inventoryIndex = index }

    if (selected == null) return

    val slot = arctic.items.selectedSlot(selected)
    if (slot < 0) return

    val newSelectIndex = newItem.inventoryIndex ?: return

    arctic.items.select(newSelectIndex, slot)
}

fun StoredDataState.deleteItem(targetItem: Item) {
    arctic.items.unselect(targetItem)

    items.remove(targetItem)
    items.filter(unequipped).forEachIndexed { index, item -> item.inventoryIndex = index }
}

fun VariantFilterIcon(with: String, selected: Boolean): ImageBitmap {
    val filename = when (with) {
        "Melee" -> "filter_melee"
        "Ranged" -> "filter_ranged"
        "Armor" -> "filter_armour"
        "Artifact" -> "filter_consume"
        "Enchanted" -> "filter_enchant"
        else -> throw RuntimeException("unreachable error!")
    }
    val suffix = if (selected) "" else "_default"
    return GameResources.image { "/Game/UI/Materials/Inventory2/Filter/$filename$suffix.png" }
}

fun RarityFilterOverlayIcon(variant: String?): ImageBitmap {
    val filename = when (variant) {
        "Melee" -> "drops_melee"
        "Ranged" -> "drops_ranged"
        "Armor" -> "drops_armour"
        "Artifact" -> "drops_item"
        else -> "drop_unknown"
    }
    return GameResources.image { "/Game/UI/Materials/MissionSelectMap/inspector/loot/$filename.png" }
}

fun RarityFilterFrame(rarity: String): ImageBitmap {
    val filename = when(rarity) {
        "Common" -> "regular_frame"
        "Rare" -> "rare_frame"
        "Unique" -> "Unique_frame"
        else -> "regular_frame"
    }
    return GameResources.image { "/Game/UI/Materials/Notification/$filename.png" }
}

enum class RarityColorType {
    Translucent, Opaque
}

fun RarityColor(rarity: String, type: RarityColorType): Color {
    return when (rarity) {
        "Common" ->
            when (type) {
                RarityColorType.Translucent -> Color(0x00aaaaaa)
                RarityColorType.Opaque -> Color(0xffaaaaaa)
            }
        "Rare" ->
            when (type) {
                RarityColorType.Translucent -> Color(0x3337c189)
                RarityColorType.Opaque -> Color(0xff37c189)
            }
        "Unique" ->
            when (type) {
                RarityColorType.Translucent -> Color(0x33ff7826)
                RarityColorType.Opaque -> Color(0xffff7826)
            }
        else -> throw RuntimeException("invalid rarity: $rarity")
    }
}
