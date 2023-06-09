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
        fileLoadFailed != null ||
        fileClose ||
        inventoryFull ||
        itemCreation != null ||
        itemEdition != null ||
        itemDuplication != null ||
        itemDeletion != null ||
        enchantment != null ||
        armorProperty != null
    }

    val nested by derivedStateOf { itemCreation?.preview != null }

    var fileLoadSrcSelector by mutableStateOf(false)
    var fileSaveDstSelector by mutableStateOf(false)
    var fileClose by mutableStateOf(false)
    var fileLoadFailed: String? by mutableStateOf(null)

    var inventoryFull by mutableStateOf(false)

    var itemCreation: ItemCreationOverlayState? by mutableStateOf(null)
    var itemEdition: Item? by mutableStateOf(null)
    var itemDuplication: Item? by mutableStateOf(null)
    var itemDeletion: Item? by mutableStateOf(null)

    var enchantment: ItemEnchantmentOverlayState? by mutableStateOf(null)
    var armorProperty: ItemArmorPropertyOverlayState? by mutableStateOf(null)

}

@Stable
class ItemCreationOverlayState {
    var preview: ItemData? by mutableStateOf(null)
}

@Stable
class ItemEnchantmentOverlayState(val target: Item, preview: Enchantment) {
    var preview: Enchantment by mutableStateOf(preview)
}

@Stable
class ItemArmorPropertyOverlayState(val target: Item, preview: ArmorProperty? = null) {
    var preview: ArmorProperty? by mutableStateOf(preview)
}
