package blackstone.states

import ItemData
import androidx.compose.runtime.*
import arctic
import blackstone.states.items.*
import extensions.replace
import stored

@Stable
class ArcticStates(stored: StoredDataState) {

    var view by mutableStateOf("inventory")

    val items = ItemsState(stored)

    val item = ItemState()

    val enchantments = EnchantmentsState()

    val armorProperties = ArmorPropertiesState()

    val popups = PopupsState()

    fun toggleView() {
        view = if (view == "storage") "inventory" else "storage"
    }

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
class ItemState {

    var enabled: String? by mutableStateOf(null)
        private set

    var updateTarget: Item? by mutableStateOf(null)

    var target: ItemData? by mutableStateOf(null)

    var filter: String by mutableStateOf("Melee")

    fun enable(with: String, updateTarget: Item? = null) {
        enabled = with
        filter = updateTarget?.data?.variant ?: "Melee"

        if (with == "edition" && updateTarget == null) throw RuntimeException("item selector enabled with edition mode, but update target not specified")
        this.updateTarget = updateTarget
    }

    fun disable() {
        enabled = null
        updateTarget = null
    }

}

@Stable
class ItemsState(private val stored: StoredDataState) {

    private val negativeIndexItemsFactory = listOf(
        { stored.items.find(equippedArtifact3) },
        { stored.items.find(equippedArtifact2) },
        { stored.items.find(equippedArtifact1) },
        { stored.items.find(equippedRanged) },
        { stored.items.find(equippedArmor) },
        { stored.items.find(equippedMelee) }
    )

    val selected = mutableStateListOf<Item?>(null, null)

    fun selected(item: Item?) = if (item != null) selected.contains(item) else false

    fun selectedSlot(item: Item) = selected.indexOf(item)

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
