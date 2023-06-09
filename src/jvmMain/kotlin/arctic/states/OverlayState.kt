package arctic.states

import androidx.compose.runtime.*
import dungeons.ItemData
import dungeons.states.ArmorProperty
import dungeons.states.Enchantment
import dungeons.states.Item

@Stable
class OverlayState {

    val visible by derivedStateOf {
        fileLoadSrcSelector ||
        fileSaveDstSelector ||
        fileLoadFailed ||
        inventoryFull ||
        itemCreation != null ||
        itemEdition != null ||
        itemDuplication != null ||
        itemDeletion != null ||
        enchantment != null ||
        armorProperty != null
    }

    val nested by derivedStateOf { itemCreation?.preview != null }

    val fileLoadSrcSelector by mutableStateOf(false)
    val fileSaveDstSelector by mutableStateOf(false)

    val fileLoadFailed by mutableStateOf(false)

    val inventoryFull by mutableStateOf(false)

    var itemCreation: ItemCreationOverlayState? by mutableStateOf(null)
    var itemEdition: Item? by mutableStateOf(null)
    val itemDuplication: Item? by mutableStateOf(null)
    val itemDeletion: Item? by mutableStateOf(null)

    var enchantment: ItemEnchantmentOverlayState? by mutableStateOf(null)
    var armorProperty: ItemArmorPropertyState? by mutableStateOf(null)

}

@Stable
class ItemCreationOverlayState {
    var preview: ItemData? by mutableStateOf(null)
}

@Stable
class ItemEnchantmentOverlayState(preview: Enchantment? = null) {
    var preview: Enchantment? by mutableStateOf(preview)
}

@Stable
class ItemArmorPropertyState(target: Item) {
    var preview: ArmorProperty? by mutableStateOf(null)
    var target: Item by mutableStateOf(target)
}
