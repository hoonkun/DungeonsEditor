package kiwi.hoonkun.ui.states

import androidx.compose.runtime.*
import kiwi.hoonkun.resources.Localizations

@Stable
class EditorState(
    val stored: DungeonsJsonState
) {
    val selection: SelectionState = SelectionState()

    var view: EditorView by mutableStateOf(EditorView.Inventory)

    val noSpaceInInventory get() = stored.items.size >= 300

    enum class EditorView {
        Inventory, Storage;

        val localizedName get() =
            if (this == Inventory) Localizations.UiText("inventory")
            else Localizations.UiText("storage")
        fun other() = if (this == Inventory) Storage else Inventory
    }

    @Stable
    class SelectionState {

        var primary: Item? by mutableStateOf(null)

        var secondary: Item? by mutableStateOf(null)

        val hasSelection get() = primary != null || secondary != null

        fun slotOf(item: Item) = if (primary == item) Slot.Primary else if (secondary == item) Slot.Secondary else null

        fun selected(item: Item?) = item != null && (primary == item || secondary == item)

        fun select(item: Item, into: Slot, unselectIfAlreadySelected: Boolean = true) {
            if (selected(item))
                if (unselectIfAlreadySelected) return unselect(item)
                else return
            else
                if (into == Slot.Primary) primary = item
                else secondary = item
        }

        fun unselect(item: Item) {
            if (primary == item) primary = null
            if (secondary == item) secondary = null
        }

        fun replace(from: Item, new: Item) {
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
}

@Composable
fun rememberEditorState(json: DungeonsJsonState): EditorState =
    remember { EditorState(json) }
