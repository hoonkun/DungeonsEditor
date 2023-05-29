package blackstone.states

import ItemData
import androidx.compose.runtime.*
import blackstone.states.items.*
import extensions.replace
import stored

@Stable
class ArcticStates {

    var view by mutableStateOf("inventory")

    val selection = SelectionState()

    val creation = CreationState()

    val edition = EditionState()

    val deletion = DeletionState()

    val duplication = DuplicationState()

    val enchantments = EnchantmentsState()

    val armorProperties = ArmorPropertiesState()

    val popups = PopupsState()

    fun toggleView() {
        view = if (view == "storage") "inventory" else "storage"
    }

}

@Stable
class DeletionState {

    var target: Item? by mutableStateOf(null)

}

@Stable
class DuplicationState {

    var target: Item? by mutableStateOf(null)

}

@Stable
class PopupsState {

    var inventoryFull by mutableStateOf(false)

    fun checkInventoryFull(): Boolean {
        if (stored.items.size >= 300) {
            inventoryFull = true
            return true
        }
        return false
    }

}

@Stable
class CreationState {

    var enabled: Boolean by mutableStateOf(false)
        private set

    var target: ItemData? by mutableStateOf(null)

    var filter: String by mutableStateOf("Melee")

    fun enable() {
        enabled = true
        filter = "Melee"
    }

    fun disable() {
        enabled = false
    }

}

@Stable
class EditionState {

    var target: Item? by mutableStateOf(null)
        private set

    fun enable(updateTarget: Item) {
        target = updateTarget
    }

    fun disable() {
        target = null
    }

}

@Stable
class SelectionState {

    val selected = mutableStateListOf<Item?>(null, null)

    fun selected(item: Item?) = if (item != null) selected.contains(item) else false

    fun select(item: Item, slot: Int) {
        if (selected.contains(item))
            unselect(item)
        else
            selected[slot] = item
    }

    fun replaceSelection(from: Item, new: Item?) {
        selected.replace(from, new)
    }

    fun unselect(item: Item) {
        selected.replace(item, null)
    }

    fun clearSelection() {
        selected[0] = null
        selected[1] = null
    }

}

@Stable
class EnchantmentsState {

    var detailTarget: Enchantment? by mutableStateOf(null)
        private set

    var shadowDetailTarget: Enchantment? by mutableStateOf(null)
        private set

    val hasDetailTarget get() = detailTarget != null

    fun isDetailTarget(enchantment: Enchantment) = detailTarget == enchantment

    fun viewDetail(enchantment: Enchantment) {
        detailTarget = enchantment
        shadowDetailTarget = enchantment
    }

    fun closeDetail() {
        detailTarget = null
    }

}

@Stable
class ArmorPropertiesState {

    var detailTarget: ArmorProperty? by mutableStateOf(null)
        private set
    var created: Boolean by mutableStateOf(false)
        private set

    val hasDetailTarget get() = detailTarget != null

    var createInto: Item? by mutableStateOf(null)
        private set

    val hasCreateInto get() = createInto != null

    fun isDetailTarget(armorProperty: ArmorProperty) = detailTarget == armorProperty

    fun viewDetail(armorProperty: ArmorProperty) {
        detailTarget = armorProperty
        createInto = null
        created = true
    }

    fun closeDetail() {
        detailTarget = null
    }

    fun requestCreate(into: Item) {
        detailTarget = null
        created = false
        createInto = into
    }

    fun cancelCreation() {
        createInto = null
    }

}
