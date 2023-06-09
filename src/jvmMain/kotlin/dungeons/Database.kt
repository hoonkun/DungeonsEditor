package dungeons

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.serialization.Serializable
import java.awt.Color
import java.awt.image.BufferedImage

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
@Immutable
data class ItemData(
    val type: String,
    val dataPath: String,
    val variant: String
) {

    val unique by lazy { variant != "Artifact" && listOf("_Unique", "_Spooky", "_Winter", "_Year").any { type.contains(it) } }

    val limited by lazy { listOf("_Spooky", "_Winter", "_Year").any { type.contains(it) } }

    val builtInProperties by lazy { Database.armorProperties.filter { it.defaultIn.contains(type) } }

    val name get() = Localizations["ItemType/${Localizations.ItemNameCorrections[type] ?: type}"]
    val flavour get() = Localizations["ItemType/Flavour_${Localizations.ItemFlavourCorrections[type] ?: type}"]
    val description get() = Localizations["ItemType/Desc_${Localizations.ItemDescriptionCorrections[type] ?: type}"]

    val inventoryIcon: ImageBitmap by lazy {
        retrieveImage("Inventory") { it.endsWith("_icon_inventory") }
    }

    val largeIcon: ImageBitmap by lazy {
        retrieveImage("Large", fallback = { it.endsWith("_icon_inventory") }) { it.endsWith("_icon") }
    }

    private fun retrieveImage(key: String, fallback: ((String) -> Boolean)? = null, criteria: (String) -> Boolean): ImageBitmap {
        val cacheKey = "$type-$key"
        val cached = IngameImages.cached(cacheKey)
        if (cached != null) return cached

        val imagePath = dataPath.let { "/Dungeons/Content".plus(it.removePrefix("/Game")) }

        val indexes = PakRegistry.index.filter { it.startsWith("$imagePath/") }

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
@Immutable
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
        if (id == "Unset") Localizations.UiText("enchantment_unset")
        else Localizations["Enchantment/${Localizations.EnchantmentDescriptionCorrections[id] ?: id}_desc"]

    val effect: String? get() =
        if (id == "Unset") Localizations.UiText("enchantment_unset_effect")
        else Localizations[Localizations.EnchantmentFixedEffectCorrections[id] ?: "Enchantment/${Localizations.EnchantmentEffectCorrections[id] ?: id}_effect"]

    val icon: ImageBitmap by lazy {
        retrieveImage(id) { it.startsWith("t_") && it.endsWith("_icon") && !it.endsWith("shine_icon") }
            ?: throw RuntimeException("no image resource found: {$id}!")
    }

    private val shinePattern: ImageBitmap? by lazy {
        retrieveImage("${id}_shine") { it.startsWith("t_") && it.endsWith("shine_icon") }
    }

    private val shinePatternR: ImageBitmap? by lazy { filterShineColor(2) }
    private val shinePatternG: ImageBitmap? by lazy { filterShineColor(1) }
    private val shinePatternB: ImageBitmap? by lazy { filterShineColor(0) }

    val shinePatterns: List<ImageBitmap>? by lazy {
        val r = shinePatternR
        val g = shinePatternG
        val b = shinePatternB
        if (r != null && g != null && b != null) listOf(r, g, b)
        else null
    }

    private fun filterShineColor(channel: Int): ImageBitmap? {
        val original = shinePattern?.toAwtImage() ?: return null
        val new = BufferedImage(original.width, original.height, original.type)
        val pixels = original.getRGB(0, 0, original.width, original.height, null, 0, original.width)
        val mask = (0xff000000u or (0xffu shl (channel * 2 * 4)))
        for (i in pixels.indices) {
            val filtered = pixels[i].toUInt() and mask
            val value = (filtered and 0x00ffffffu) shr (channel * 2 * 4)
            pixels[i] = ((value shl 6 * 4) or (value shl 4 * 4) or (value shl 2 * 4) or value).toInt()
        }
        new.setRGB(0, 0, original.width, original.height, pixels, 0, original.width)
        return new.toComposeImageBitmap()
    }

    private fun retrieveImage(cacheKey: String, criteria: (String) -> Boolean): ImageBitmap? {
        if (id == "Unset") return IngameImages.get("EnchantmentUnset") { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png" }

        val cached = IngameImages.cached(cacheKey)
        if (cached != null) return cached

        val imagePath = Database.enchantment(id)?.dataPath?.let { "/Dungeons/Content".plus(it.removePrefix("/Game")) }
            ?: throw RuntimeException("unknown enchantment id!")

        val indexes = PakRegistry.index.filter { it.startsWith(imagePath) }
        val pakPath = indexes.find { criteria(it.replaceBeforeLast('/', "").removePrefix("/").replaceAfterLast(".", "").removeSuffix(".").lowercase()) }

        val preprocess: (BufferedImage) -> BufferedImage =
            if (cacheKey.endsWith("_shine")) {
                {
                    val newImage = BufferedImage(it.width, it.height, BufferedImage.TYPE_INT_RGB)
                    val graphics = newImage.createGraphics()
                    graphics.color = Color.BLACK
                    graphics.fillRect(0, 0, newImage.width, newImage.height)
                    graphics.drawImage(it, 0, 0, null)
                    graphics.dispose()
                    newImage
                }
            } else {
                { it }
            }

        return if (pakPath != null) IngameImages.get(cacheKey, preprocess) { pakPath } else null
    }

    companion object {
        val CommonNonGlidedInvestedPoints = listOf(1, 2, 3)
        val PowerfulNonGlidedInvestedPoints = listOf(2, 3, 4)
        val CommonGlidedInvestedPoints = listOf(2, 3, 4)
        val PowerfulGlidedInvestedPoints = listOf(3, 4, 5)
    }

}

@Serializable
@Immutable
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
