package arctic.states

import androidx.compose.runtime.*
import dungeons.Localizations
import dungeons.states.DungeonsJsonState
import dungeons.states.Item

@Stable
class EditorState(json: DungeonsJsonState) {

    val stored: DungeonsJsonState = json

    val selection: EditorSelectionState = EditorSelectionState()

    var view: EditorView by mutableStateOf(EditorView.Inventory)

    val noSpaceInInventory by derivedStateOf { stored.items.size >= 300 }

    enum class EditorView {
        Inventory, Storage;

        val localizedName get() = if (this == Inventory) Localizations.UiText("inventory") else Localizations.UiText("storage")
        fun other() = if (this == Inventory) Storage else Inventory
    }

}

@Stable
class EditorSelectionState {

    var primary: Item? by mutableStateOf(null)

    var secondary: Item? by mutableStateOf(null)

    val hasSelection by derivedStateOf { primary != null || secondary != null }

    fun selected(item: Item?) = item != null && (primary == item || secondary == item)

    fun select(item: Item, into: EditorSelectionSlot) {
        if (selected(item)) return unselect(item)
        if (into == EditorSelectionSlot.Primary) primary = item
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

    enum class EditorSelectionSlot {
        Primary, Secondary
    }

}
