package states

import androidx.compose.runtime.*

@Stable
class EditorState(stored: StoredFileState) {
    val inventoryState: InventoryEditorState = InventoryEditorState(stored)
    val detailState: DetailEditorState = DetailEditorState()
}

@Stable
class InventoryEditorState(stored: StoredFileState) {
    val selectedIndexes = mutableStateListOf<Int?>(null, null)
    val selectedItems by derivedStateOf {
        selectedIndexes.map map@ { captured ->
            if (captured == null) return@map null

            val reservedNegativeIndexes = listOf(
                stored.items.hotbar3,
                stored.items.hotbar2,
                stored.items.hotbar1,
                stored.items.ranged,
                stored.items.armor,
                stored.items.melee
            )
            if (captured < 0) return@map reservedNegativeIndexes[captured + 6]

            return@map stored.items.unequipped.find { it.inventoryIndex == captured }
        }
    }

    fun select(index: Int, by: String) {
        when (by) {
            "primary" -> selectedIndexes[0] = if (selectedIndexes[0] == index) null else index
            "secondary" -> selectedIndexes[1] = if (selectedIndexes[1] == index) null else index
        }
    }
}

@Stable
class DetailEditorState {
    var selectedEnchantment: Enchantment? by mutableStateOf(null)
        private set

    var enchantmentSelectorOpen: Boolean by mutableStateOf(false)
        private set

    var selectedArmorProperty: ArmorProperty? by mutableStateOf(null)

    var armorPropertySelectorOpen: Boolean by mutableStateOf(false)

    fun toggleEnchantmentSelector() {
        enchantmentSelectorOpen = !enchantmentSelectorOpen
    }

    fun selectEnchantment(enchantment: Enchantment) {
        if (selectedArmorProperty != null) unselectArmorProperty()

        enchantmentSelectorOpen = false
        selectedEnchantment =
            if (selectedEnchantment == enchantment) null
            else enchantment
    }

    fun unselectEnchantment() {
        selectedEnchantment = null
        enchantmentSelectorOpen = false
    }

    fun selectArmorProperty(property: ArmorProperty) {
        if (selectedEnchantment != null) unselectEnchantment()

        armorPropertySelectorOpen = false
        selectedArmorProperty =
            if (selectedArmorProperty == property) null
            else property
    }

    fun unselectArmorProperty() {
        selectedArmorProperty = null
        armorPropertySelectorOpen = false
    }

    fun toggleArmorPropertySelector() {
        armorPropertySelectorOpen = !armorPropertySelectorOpen
    }
}
