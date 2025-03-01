package minecraft.dungeons.states.extensions

import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.values.DungeonsItem


object MutableDungeonsItemsExtensionScope {

    val MutableDungeons.noSpaceAvailable get() = allItems.size >= 300

    fun MutableDungeons.locationOf(item: MutableDungeons.Item): DungeonsItem.Location {
        return if (allItems.contains(item)) DungeonsItem.Location.Inventory
        else if (storageItems.contains(item)) DungeonsItem.Location.Storage
        else throw RuntimeException("This item is not exists in any available space.")
    }

    fun MutableDungeons.add(
        newItem: MutableDungeons.Item,
        where: DungeonsItem.Location
    ): MutableDungeons.Item {
        when (where) {
            DungeonsItem.Location.Inventory -> {
                allItems.add(6.coerceAtMost(allItems.size), newItem)
                inventoryItems.forEachIndexed { index, item -> item.inventoryIndex = index }
            }
            DungeonsItem.Location.Storage -> {
                storageItems.add(0, newItem)
                storageItems.forEachIndexed { index, item -> item.inventoryIndex = index }
            }
        }
        return newItem
    }

    fun MutableDungeons.duplicate(
        targetItem: MutableDungeons.Item
    ): MutableDungeons.Item {
        val newItem = targetItem.copy()
        add(newItem, locationOf(targetItem))
        return newItem
    }

    fun MutableDungeons.remove(
        targetItem: MutableDungeons.Item
    ) {
        when (locationOf(targetItem)) {
            DungeonsItem.Location.Inventory -> {
                allItems.remove(targetItem)
                inventoryItems.forEachIndexed { index, item -> item.inventoryIndex = index }
            }
            DungeonsItem.Location.Storage -> {
                storageItems.remove(targetItem)
                storageItems.forEachIndexed { index, item -> item.inventoryIndex = index }
            }
        }
    }

    fun MutableDungeons.transfer(
        item: MutableDungeons.Item
    ): DungeonsItem.Location {
        val previousLocation = locationOf(item)

        if (previousLocation == DungeonsItem.Location.Inventory) {
            allItems.remove(item)
            storageItems.add(0, item)
        } else {
            storageItems.remove(item)
            allItems.add(0, item)
        }
        inventoryItems.forEachIndexed { index, existing -> existing.inventoryIndex = index }
        storageItems.forEachIndexed { index, existing -> existing.inventoryIndex = index }

        return previousLocation.other()
    }

}

fun <T>withItemManager(block: MutableDungeonsItemsExtensionScope.() -> T) = MutableDungeonsItemsExtensionScope.block()
