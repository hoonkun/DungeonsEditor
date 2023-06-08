package dungeons

import Constants
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Database(
    val armorProperties: List<ArmorPropertyData>,
    val enchantments: List<EnchantmentData>,
    val items: List<ItemData>,
) {

    companion object {

        private var _current: Database? = null
        private val current get() = _current!!

        val armorProperties get() = current.armorProperties
        val enchantments get() = current.enchantments
        val items get() = current.items

        fun register(database: Database) {
            if (_current != null) return
            _current = database
        }

        fun item(type: String) = items.find { it.type == type }

        fun enchantment(id: String) = enchantments.find { it.id == id }

        fun armorProperty(id: String) = armorProperties.find { it.id == id }

    }

}

@Serializable
data class ItemData(
    val type: String,
    val dataPath: String,
    val variant: String
) {

    val unique get() = variant != "Artifact" && listOf("_Unique", "_Spooky", "_Winter", "_Year").any { type.contains(it) }

    val limited get() = listOf("_Spooky", "_Winter", "_Year").any { type.contains(it) }

    val builtInProperties get() = Database.armorProperties.filter { it.defaultIn.contains(type) }

    val name get() = Localizations["ItemType/${Localizations.ItemNameCorrections[type] ?: type}"]
    val flavour get() = Localizations["ItemType/Flavour_${Localizations.ItemFlavourCorrections[type] ?: type}"]
    val description get() = Localizations["ItemType/Desc_${Localizations.ItemDescriptionCorrections[type] ?: type}"]

    val inventoryIcon: ImageBitmap get() =
        retrieveImage("Inventory") { it.endsWith("_icon_inventory") }

    val largeIcon: ImageBitmap get() =
        retrieveImage("Large", fallback = { it.endsWith("_icon_inventory") }) { it.endsWith("_icon") }

    private fun retrieveImage(key: String, fallback: ((String) -> Boolean)? = null, criteria: (String) -> Boolean): ImageBitmap {
        val cacheKey = "$type-$key"
        val cached = IngameImages.cached(cacheKey)
        if (cached != null) return cached

        val imagePath = Database.item(type)?.dataPath?.let { "/Dungeons/Content".plus(it.removePrefix("/Game")) }
            ?: throw RuntimeException("unknown item type!")

        val indexes = PakRegistry.index.filter { it.startsWith(imagePath) }

        val candidate1 = indexes.find { criteria(it.lowercase().replaceAfterLast('.', "").removeSuffix(".")) }
        if (candidate1 != null)
            return IngameImages.get(cacheKey) { candidate1 }

        if (fallback == null) throw RuntimeException("no image resource found with item $type, which type is $key")

        val candidate2 = indexes.find { fallback(it.lowercase().replaceAfterLast('.', "").removeSuffix(".")) }
        if (candidate2 != null)
            return IngameImages.get(cacheKey) { candidate2 }

        throw RuntimeException("no image resource found with item $type, which type is $key")
    }

}

@Serializable
data class EnchantmentData(
    val id: String,
    val dataPath: String,
    val powerful: Boolean = false,
    val multipleAllowed: Boolean = false,
    val applyFor: Set<String>? = null,
    val applyExclusive: List<String>? = null,
    val specialDescValues: List<String>? = null
) {

    val name: String get() =
        Localizations["Enchantment/${Localizations.EnchantmentNameCorrections[id] ?: id}"] ?: "???"

    val description: String? get() =
        if (id == "Unset") "이 슬롯을 비활성화 상태로 변경합니다.\n'화려한'에 설정된 효과 부여의 경우 금박이 지워진 상태로 변경됩니다."
        else Localizations["Enchantment/${Localizations.EnchantmentDescriptionCorrections[id] ?: id}_desc"]

    val effect: String? get() =
        if (id == "Unset") "{0} 확률로 아무것도 하지 않습니다?"
        else Localizations[Localizations.EnchantmentFixedEffectCorrections[id] ?: "Enchantment/${Localizations.EnchantmentEffectCorrections[id] ?: id}_effect"]

    val iconScale: Float get() =
        if (id == "Unset") 1.05f
        else 1.425f

    val icon: ImageBitmap get() =
        retrieveImage(id) { it.startsWith("t_") && it.endsWith("_icon") && !it.endsWith("shine_icon") }
            ?: throw RuntimeException("no image resource found: {$id}!")

    val shinePattern: ImageBitmap? get() =
        retrieveImage("${id}_shine") { it.startsWith("t_") && it.endsWith("shine_icon") }

    private fun retrieveImage(cacheKey: String, criteria: (String) -> Boolean): ImageBitmap? {
        if (id == "Unset") return IngameImages.get("EnchantmentUnset") { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png" }

        val cached = IngameImages.cached(cacheKey)
        if (cached != null) return cached

        val imagePath = Database.enchantment(id)?.dataPath?.let { "/Dungeons/Content".plus(it.removePrefix("/Game")) }
            ?: throw RuntimeException("unknown enchantment id!")

        val indexes = PakRegistry.index.filter { it.startsWith(imagePath) }
        val pakPath = indexes.find { criteria(it.replaceBeforeLast('/', "").removePrefix("/").replaceAfterLast(".", "").removeSuffix(".").lowercase()) }

        return if (pakPath != null) IngameImages.get(cacheKey) { pakPath } else null
    }

    companion object {
        val CommonNonGlidedInvestedPoints = listOf(1, 2, 3)
        val PowerfulNonGlidedInvestedPoints = listOf(2, 3, 4)
        val CommonGlidedInvestedPoints = listOf(2, 3, 4)
        val PowerfulGlidedInvestedPoints = listOf(3, 4, 5)
    }

}

@Serializable
data class ArmorPropertyData(
    val id: String,
    val defaultIn: List<String> = emptyList()
) {

    val description get() =
        (Localizations["ArmorProperties/${Localizations.ArmorPropertyCorrections[id] ?: id}_description"])
            ?.replace("{0}", "")
            ?.replace("{1}", "")
            ?.replace("{2}", "")
            ?.replace("개의", "추가")
            ?.replace("만큼", "")
            ?.replace("  ", " ")
            ?.trim()

}
