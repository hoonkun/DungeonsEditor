package kiwi.hoonkun.ui.states

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import minecraft.dungeons.io.DungeonsJsonFile
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.values.DungeonsItem

@Stable
class EditorState(
    val source: DungeonsJsonFile,
    val data: MutableDungeons
) {
    private val selections: SnapshotStateMap<Slot, MutableDungeons.Item?> = mutableStateMapOf<Slot, MutableDungeons.Item?>()
        .apply { Slot.entries.forEach { this[it] = null } }

    var view: DungeonsItem.Location by mutableStateOf(DungeonsItem.Location.Inventory)

    var isInTowerEditMode: Boolean by mutableStateOf(false)
    var towerEditEnabled by mutableStateOf(false)

    val primary: MutableDungeons.Item? get() = selections[Slot.Primary]
    val secondary: MutableDungeons.Item? get() = selections[Slot.Secondary]

    val hasSelection get() = selections.values.any { it != null }

    fun selectedSlotOf(item: MutableDungeons.Item) =
        selections.entries.find { it.value == item }?.key

    fun selected(item: MutableDungeons.Item) =
        selections.values.contains(item)

    fun select(
        item: MutableDungeons.Item,
        into: Slot,
        unselectIfAlreadySelected: Boolean = true
    ) {
        if (!selected(item))
            selections[into] = item
        else if (unselectIfAlreadySelected)
            deselect(item)
    }

    fun deselect(item: MutableDungeons.Item) =
        selectedSlotOf(item)?.also { selections[it] = null }

    fun deselectAll() =
        selections.clear()

    fun reselect(oldItem: MutableDungeons.Item, newItem: MutableDungeons.Item) =
        selectedSlotOf(oldItem)?.also { selections[it] = newItem }

    enum class Slot {
        Primary, Secondary
    }
}

fun EditorState(path: String) =
    DungeonsJsonFile(path).let { EditorState(it, MutableDungeons(it.read())) }
