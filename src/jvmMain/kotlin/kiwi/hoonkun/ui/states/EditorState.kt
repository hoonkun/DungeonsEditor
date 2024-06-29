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
    val selection: SelectionState = SelectionState()

    var view: DungeonsItem.Location by mutableStateOf(DungeonsItem.Location.Inventory)

    @Stable
    class SelectionState {

        private val selections: SnapshotStateMap<Slot, MutableDungeons.Item?> = mutableStateMapOf<Slot, MutableDungeons.Item?>()
            .apply { Slot.entries.forEach { this[it] = null } }

        val primary: MutableDungeons.Item? get() = selections[Slot.Primary]
        val secondary: MutableDungeons.Item? get() = selections[Slot.Secondary]

        val hasSelection get() = selections.values.any { it != null }

        fun slotOf(item: MutableDungeons.Item) =
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
            slotOf(item)?.also { selections[it] = null }

        fun replace(oldItem: MutableDungeons.Item, newItem: MutableDungeons.Item) =
            slotOf(oldItem)?.also { selections[it] = newItem }

        fun clear() =
            selections.clear()

        enum class Slot {
            Primary, Secondary
        }
    }

    companion object {
        fun fromPath(path: String) =
            DungeonsJsonFile(path).let { EditorState(it, MutableDungeons(it.read())) }
    }
}
