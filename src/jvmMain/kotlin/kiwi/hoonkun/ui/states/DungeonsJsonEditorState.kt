package kiwi.hoonkun.ui.states

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kiwi.hoonkun.resources.Localizations
import minecraft.dungeons.io.DungeonsJsonFile
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.values.DungeonsItem

@Stable
class DungeonsJsonEditorState(
    val source: DungeonsJsonFile,
    val stored: MutableDungeons
) {
    val selection: SelectionState = SelectionState()

    var view: EditorView by mutableStateOf(EditorView.Inventory)

    enum class EditorView {
        Inventory, Storage;

        val localizedName get() = if (this == Inventory) Localizations["inventory"] else Localizations["storage"]
        fun other() = if (this == Inventory) Storage else Inventory
        fun toItemLocation() = if (this == Inventory) DungeonsItem.Location.Inventory else DungeonsItem.Location.Storage

        fun isInventory() = this == Inventory
        fun isStorage() = this == Storage

        companion object {
            fun DungeonsItem.Location.toEditorView() =
                when(this) {
                    DungeonsItem.Location.Inventory -> Inventory
                    DungeonsItem.Location.Storage -> Storage
                }
        }
    }

    @Stable
    class SelectionState {

        var primary: MutableDungeons.Item? by mutableStateOf(null)

        var secondary: MutableDungeons.Item? by mutableStateOf(null)

        val hasSelection get() = primary != null || secondary != null

        fun slotOf(item: MutableDungeons.Item) = if (primary == item) Slot.Primary else if (secondary == item) Slot.Secondary else null

        fun selected(item: MutableDungeons.Item?) = item != null && (primary == item || secondary == item)

        fun select(item: MutableDungeons.Item, into: Slot, unselectIfAlreadySelected: Boolean = true) {
            if (selected(item))
                if (unselectIfAlreadySelected) return unselect(item)
                else return
            else
                if (into == Slot.Primary) primary = item
                else secondary = item
        }

        fun unselect(item: MutableDungeons.Item) {
            if (primary == item) primary = null
            if (secondary == item) secondary = null
        }

        fun replace(from: MutableDungeons.Item, new: MutableDungeons.Item) {
            if (primary == from) primary = new
            if (secondary == from) secondary = new
        }

        fun clear() {
            primary = null
            secondary = null
        }

        enum class Slot {
            Primary, Secondary
        }
    }

    companion object {
        fun fromPath(path: String) =
            DungeonsJsonFile(path).let { DungeonsJsonEditorState(it, MutableDungeons(it.read())) }
    }
}
