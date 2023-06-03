import androidx.compose.ui.graphics.ImageBitmap
import extensions.GameResources
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Database(
    val armorProperties: List<ArmorPropertyData>,
    val enchantments: List<EnchantmentData>,
    val items: List<ItemData>,
) {

    companion object {

        private val _current = load()
        private val current get() = _current!!

        val armorProperties get() = current.armorProperties
        val enchantments get() = current.enchantments
        val items get() = current.items

        fun item(type: String) = items.find { it.type == type }

        fun enchantment(id: String) = enchantments.find { it.id == id }

        fun armorProperty(id: String) = armorProperties.find { it.id == id }

        private fun load(): Database? = {}::class.java.getResource("database.json")?.readText()?.let { Json.decodeFromString<Database>(it) }

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
        retrieveImage("Inventory") { it.endsWith("_icon_inventory.png") }

    val largeIcon: ImageBitmap get() =
        retrieveImage("Large", fallback = { it.endsWith("_icon_inventory.png") }) { it.endsWith("_icon.png") }

    private fun retrieveImage(key: String, fallback: ((String) -> Boolean)? = null, criteria: (String) -> Boolean): ImageBitmap {
        val cacheKey = "$type-$key"
        val cached = GameResources.image(cacheKey)
        if (cached != null) return cached

        val imagePath = Database.item(type)?.dataPath ?: throw RuntimeException("unknown item type!")
        val files = File("${Constants.GameDataDirectoryPath}${imagePath}").listFiles()
            ?: throw RuntimeException("could not list files to retrieve item image: $type")

        val candidate1 = files.find { it.extension == "png" && criteria(it.name.lowercase()) }
        if (candidate1 != null)
            return GameResources.image(cacheKey, false) { candidate1.absolutePath }

        if (fallback == null) throw RuntimeException("no image resource found with item $type, which type is $key")

        val candidate2 = files.find { it.extension == "png" && fallback(it.name.lowercase()) }

        if (candidate2 != null)
            return GameResources.image(cacheKey, false) { candidate2.absolutePath }

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
        retrieveImage(id) { it.name.lowercase().endsWith("_icon.png") && !it.name.lowercase().endsWith("shine_icon.png") }

    private fun retrieveImage(cacheKey: String, criteria: (File) -> Boolean): ImageBitmap {
        if (id == "Unset") return GameResources.image("EnchantmentUnset") { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png" }

        val cached = GameResources.image(cacheKey)
        if (cached != null) return cached

        val imagePath = Database.enchantment(id)?.dataPath ?: throw RuntimeException("unknown enchantment id!")
        val dataDirectory = File("${Constants.GameDataDirectoryPath}${imagePath}")
        val imageFile = dataDirectory.listFiles()?.find { it.extension == "png" && criteria(it) }
            ?: throw RuntimeException("no image resource found: {$id}!")

        return GameResources.image(cacheKey, false) { imageFile.absolutePath }
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
