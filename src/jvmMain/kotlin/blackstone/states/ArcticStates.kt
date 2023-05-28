package blackstone.states

import ItemData
import androidx.compose.runtime.*
import blackstone.states.items.*

@Stable
class ArcticStates(stored: StoredDataState) {

    val items = ItemsState(stored)

    val item = ItemState()

    val enchantments = EnchantmentsState()

    val armorProperties = ArmorPropertiesState()

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

    private val selectedIndexes = mutableStateListOf<Int?>(null, null)
    val selected by derivedStateOf {
        selectedIndexes.map {
            if (it == null) null
            else if (it < 0) negativeIndexItemsFactory[it + 6]()
            else stored.items.find { item -> item.inventoryIndex == it }
        }
    }

    fun selected(index: Int) = selectedIndexes.contains(index)

    fun selected(item: Item?) = selected.contains(item)

    fun selectedSlot(item: Item) = selected.indexOf(item)

    fun select(index: Int, slot: Int) {
        selectedIndexes[slot] = if (selectedIndexes[slot] == index) null else index
    }

    fun unselect(item: Item) {
        val index = selected.indexOf(item)
        if (index < 0) return

        selectedIndexes[index] = null
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
