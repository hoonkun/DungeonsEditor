package dungeons.states.extensions

import dungeons.states.DungeonsJsonState
import dungeons.states.Item
import arctic.states.arctic
import dungeons.DungeonsLevel
import dungeons.DungeonsPower

fun DungeonsJsonState.addItem(newItem: Item, copiedFrom: Item? = null) {
    val where = copiedFrom?.where ?: arctic.view
    if (where == "inventory") {
        items.add(6, newItem)
        unequippedItems.forEachIndexed { index, item -> item.inventoryIndex = index }
    } else {
        storageChestItems.add(0, newItem)
        storageChestItems.forEachIndexed { index, item -> item.inventoryIndex = index }
    }

    if (copiedFrom != null) {
        arctic.selection.replaceSelection(copiedFrom, newItem)
    } else {
        arctic.selection.clearSelection()
        arctic.selection.select(newItem, 0)
    }
}

fun DungeonsJsonState.deleteItem(targetItem: Item) {
    arctic.selection.unselect(targetItem)

    when (targetItem.where) {
        "inventory" -> {
            items.remove(targetItem)
            unequippedItems.forEachIndexed { index, item -> item.inventoryIndex = index }
        }
        "storage" -> {
            storageChestItems.remove(targetItem)
            storageChestItems.forEachIndexed { index, item -> item.inventoryIndex = index }
        }
        else -> { /* This item does not exist in any available spaces. */ }
    }
}

val DungeonsJsonState.playerLevel: Double get() = DungeonsLevel.toInGameLevel(xp)

val DungeonsJsonState.totalSpentEnchantmentPoints: Int get() =
    items.sumOf { it.enchantments?.sumOf { en -> en.investedPoints } ?: 0 } + storageChestItems.sumOf { it.enchantments?.sumOf { en -> en.investedPoints } ?: 0 }

val DungeonsJsonState.playerPower: Int get() {
    val powerDividedBy4 = listOf(
        items.find(equippedMelee),
        items.find(equippedArmor),
        items.find(equippedRanged)
    ).sumOf { DungeonsPower.toInGamePower(it?.power ?: 0.0) } / 4.0

    val powerDividedBy12 = listOf(
        items.find(equippedArtifact1),
        items.find(equippedArtifact2),
        items.find(equippedArtifact3)
    ).sumOf { DungeonsPower.toInGamePower(it?.power ?: 0.0) } / 12.0

    return (powerDividedBy4 + powerDividedBy12).toInt()
}

val DungeonsJsonState.equippedItems get() = listOf(
    items.find(equippedMelee),
    items.find(equippedArmor),
    items.find(equippedRanged),
    items.find(equippedArtifact1),
    items.find(equippedArtifact2),
    items.find(equippedArtifact3)
)

val DungeonsJsonState.unequippedItems get() = items.filter { it.inventoryIndex != null }

private val equippedMelee: (Item) -> Boolean = { it.equipmentSlot == "MeleeGear" }
private val equippedRanged: (Item) -> Boolean = { it.equipmentSlot == "RangedGear" }
private val equippedArmor: (Item) -> Boolean = { it.equipmentSlot == "ArmorGear" }
private val equippedArtifact1: (Item) -> Boolean = { it.equipmentSlot == "HotbarSlot1" }
private val equippedArtifact2: (Item) -> Boolean = { it.equipmentSlot == "HotbarSlot2" }
private val equippedArtifact3: (Item) -> Boolean = { it.equipmentSlot == "HotbarSlot3" }
