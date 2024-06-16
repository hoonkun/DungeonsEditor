package dungeons.states.extensions

import arctic.states.EditorSelectionState
import arctic.states.EditorState
import dungeons.states.DungeonsJsonState
import dungeons.states.Item

fun DungeonsJsonState.addItem(editor: EditorState, newItem: Item, copiedFrom: Item? = null) {
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
        editor.selection.select(newItem, EditorSelectionState.Slot.Primary)
    }
}

fun DungeonsJsonState.deleteItem(editor: EditorState, targetItem: Item) {
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
