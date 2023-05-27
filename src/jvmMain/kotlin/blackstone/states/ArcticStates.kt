package blackstone.states

import androidx.compose.runtime.*
import blackstone.states.items.*

@Stable
class ArcticStates(stored: StoredDataState) {

    val items = ItemsState(stored)

    val enchantments = EnchantmentsState(stored)

    val armorProperties = ArmorPropertiesState(stored)

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

    fun select(index: Int, slot: Int) {
        selectedIndexes[slot] = if (selectedIndexes[slot] == index) null else index
    }

}

@Stable
class EnchantmentsState(private val stored: StoredDataState) {

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
class ArmorPropertiesState(private val stored: StoredDataState) {

    var detailTarget: ArmorProperty? by mutableStateOf(null)
        private set

    val hasDetailTarget get() = detailTarget != null

    private var createInto: Item? by mutableStateOf(null)

    val hasCreateInto get() = createInto != null

    fun isDetailTarget(armorProperty: ArmorProperty) = detailTarget == armorProperty

    fun viewDetail(armorProperty: ArmorProperty) {
        detailTarget = armorProperty
        createInto = null
    }

    fun closeDetail() {
        detailTarget = null
    }

    fun requestCreate(into: Item) {
        detailTarget = null
        createInto = into
    }

    fun cancelCreation() {
        createInto = null
    }

}
