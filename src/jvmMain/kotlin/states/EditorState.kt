package states

import androidx.compose.runtime.*
import blackstone.states.StoredDataState
import blackstone.states.items.*
import blackstone.states.Enchantment
import blackstone.states.ArmorProperty

@Stable
class EditorState(stored: StoredDataState) {
    val inventory: InventoryEditorState = InventoryEditorState(stored)
    val detail: DetailEditorState = DetailEditorState()
}

@Stable
class InventoryEditorState(stored: StoredDataState) {
    val selectedIndexes = mutableStateListOf<Int?>(null, null)
    val selectedItems by derivedStateOf {
        selectedIndexes.map map@ { captured ->
            if (captured == null) return@map null

            val reservedNegativeIndexes = listOf(
                stored.items.find(equippedArtifact3),
                stored.items.find(equippedArtifact2),
                stored.items.find(equippedArtifact1),
                stored.items.find(equippedRanged),
                stored.items.find(equippedArmor),
                stored.items.find(equippedMelee)
            )
            if (captured < 0) return@map reservedNegativeIndexes[captured + 6]

            return@map stored.items.filter(unequipped).find { it.inventoryIndex == captured }
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

    var selectedArmorProperty: ArmorProperty? by mutableStateOf(null)
        private set

    fun toggleEnchantment(enchantment: Enchantment) {
        if (selectedArmorProperty != null) unselectArmorProperty()

        selectedEnchantment =
            if (selectedEnchantment == enchantment) null
            else enchantment
    }

    fun unselectEnchantment() {
        selectedEnchantment = null
    }

    fun toggleArmorProperty(property: ArmorProperty) {
        if (selectedEnchantment != null) unselectEnchantment()

        selectedArmorProperty =
            if (selectedArmorProperty == property) null
            else property
    }

    fun unselectArmorProperty() {
        selectedArmorProperty = null
    }
}
