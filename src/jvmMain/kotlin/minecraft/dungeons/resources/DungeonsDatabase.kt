package minecraft.dungeons.resources

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.utils.ResourceReadable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import minecraft.dungeons.io.DungeonsPakRegistry
import pak.PakIndex
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

object DungeonsDatabase {
    var armorProperties: Set<ArmorPropertyData> = emptySet()
        private set
    var enchantments: Set<EnchantmentData> = emptySet()
        private set
    var items: Set<ItemData> = emptySet()
        private set

    fun item(type: String) = items.find { it.type == type }

    fun enchantment(id: String) = enchantments.find { it.id == id }

    fun armorProperty(id: String) = armorProperties.find { it.id == id }

    object Initializer: ResourceReadable() {

        private val KeyMappings: Map<String, String> =
            Json.decodeFromString(resourceText("databases/key_mappings.json"))

        private val ExcludedItems =
            Json.decodeFromString<List<String>>(resourceText("databases/excluded_items.json")).toSet()

        private val armorProperties =
            Json.decodeFromString<Map<String, List<String>>>(resourceText("databases/armor_properties.json"))
                .map { (k, v) -> ArmorPropertyData(k, v) }
                .toSet()

        fun run(index: PakIndex) {
            val items = mutableSetOf<ItemData>()
            val enchantments = mutableSetOf<EnchantmentData>()

            index.forEach { pathString ->
                val path = Path(pathString)
                val dataPath = "/Game".plus(path.parent.pathString.replace("\\", "/").removePrefix("/Dungeons/Content"))

                if (pathString.contains("ArmorProperties") && !pathString.contains("Cues")) {
                    return@forEach
                }

                if (pathString.contains("data") && pathString.contains("levels")) {
                    return@forEach
                }

                if (!path.name.startsWith("T") || !pathString.lowercase().contains("_icon")) {
                    return@forEach
                }

                if (pathString.contains("Enchantments") && path.nameWithoutExtension.endsWith("_icon", true)) {
                    val enchantmentKey = correctKey(path.parent.name)
                    val enchantment = EnchantmentData(enchantmentKey, dataPath)
                    if (enchantments.contains(enchantment)) return@forEach

                    enchantments.add(enchantment)
                }

                if (path.nameWithoutExtension.endsWith("_icon_inventory", true)) {
                    if (ExcludedItems.any { path.parent.name == it }) return@forEach

                    val itemKey = correctKey(path.parent.name)
                    val type =
                        if (dataPath.contains("MeleeWeapons")) "Melee"
                        else if (dataPath.contains("RangedWeapons")) "Ranged"
                        else if (dataPath.contains("Armor")) "Armor"
                        else null

                    if (type != null) {
                        val gear = ItemData(itemKey, dataPath, type)
                        if (!items.contains(gear)) items.add(gear)
                    } else if (pathString.contains("Items")) {
                        val artifact = ItemData(itemKey, dataPath, "Artifact")
                        items.removeIf { artifact.type == it.type }
                        items.add(artifact)
                    }
                }

            }

            enchantments.add(EnchantmentData(id = "Unset", ""))

            DungeonsDatabase.items = items
            DungeonsDatabase.enchantments = enchantments
            DungeonsDatabase.armorProperties = armorProperties
        }

        private fun correctKey(key: String): String {
            return KeyMappings[key] ?: key
        }
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

    val builtInProperties by lazy { DungeonsDatabase.armorProperties.filter { it.defaultIn.contains(type) } }

    val name get() = DungeonsLocalizations["ItemType/${DungeonsLocalizations.Corrections.ItemName[type] ?: type}"]
    val flavour get() = DungeonsLocalizations["ItemType/Flavour_${DungeonsLocalizations.Corrections.ItemFlavour[type] ?: type}"]
    val description get() = DungeonsLocalizations["ItemType/Desc_${DungeonsLocalizations.Corrections.ItemDescription[type] ?: type}"]

    val inventoryIcon: ImageBitmap by lazy {
        retrieveImage("Inventory") { it.endsWith("_icon_inventory") }
    }

    val largeIcon: ImageBitmap by lazy {
        retrieveImage("Large", fallback = { it.endsWith("_icon_inventory") }) { it.endsWith("_icon") }
    }

    val isArtifact = variant == "Artifact"

    fun load() {
        inventoryIcon
        largeIcon
    }

    private fun retrieveImage(
        key: String,
        fallback: ((String) -> Boolean)? = null,
        criteria: (String) -> Boolean
    ): ImageBitmap {
        val cacheKey = "$type-$key"
        val cached = DungeonsTextures.cached(cacheKey)
        if (cached != null) return cached

        val imagePath = dataPath.let { "/Dungeons/Content".plus(it.removePrefix("/Game")) }

        val indexes = DungeonsPakRegistry.index.filter { it.startsWith("$imagePath/") }

        val candidate1 = indexes.find { criteria(it.lowercase().replaceAfterLast('.', "").removeSuffix(".")) }
        if (candidate1 != null)
            return DungeonsTextures.get(cacheKey) { candidate1 }

        if (fallback == null) throw RuntimeException("no image resource found with item $type, which type is $key")

        val candidate2 = indexes.find { fallback(it.lowercase().replaceAfterLast('.', "").removeSuffix(".")) }
        if (candidate2 != null)
            return DungeonsTextures.get(cacheKey) { candidate2 }

        throw RuntimeException("no image resource found with item $type, which type is $key")
    }

}

@Serializable
@Immutable
data class EnchantmentData(
    val id: String,
    val dataPath: String,
) {
    val powerful = PowerfulEnchantments.contains(id)
    val stackable = StackableEnchantments.contains(id)
    val applyFor = mutableSetOf<String>()
        .apply {
            if (ArmorEnchantments.contains(id)) add("Armor")
            if (MeleeEnchantments.contains(id)) add("Melee")
            if (RangedEnchantments.contains(id)) add("Ranged")
            if (ExclusiveEnchantments.containsKey(id)) add("Exclusive")
        }
        .toSet()
    val applyExclusive = ExclusiveEnchantments.getOrElse(id) { emptyList() }
    val specialDescValues = EnchantmentSpecialDescValue.getOrElse(id) { emptyList() }

    val name: String get() =
        DungeonsLocalizations["Enchantment/${DungeonsLocalizations.Corrections.EnchantmentName[id] ?: id}"] ?: "???"

    val description: String? get() =
        if (id == "Unset") Localizations["enchantment_unset"]
        else DungeonsLocalizations["Enchantment/${DungeonsLocalizations.Corrections.EnchantmentDescription[id] ?: id}_desc"]

    val effect: String? get() =
        if (id == "Unset") Localizations["enchantment_unset_effect"]
        else DungeonsLocalizations[DungeonsLocalizations.Corrections.EnchantmentFixedEffect[id] ?: "Enchantment/${DungeonsLocalizations.Corrections.EnchantmentEffect[id] ?: id}_effect"]

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

    fun load() {
        icon
        shinePatterns
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
        if (id == "Unset") return DungeonsTextures.get("EnchantmentUnset") { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png" }

        val cached = DungeonsTextures.cached(cacheKey)
        if (cached != null) return cached

        val imagePath = DungeonsDatabase.enchantment(id)?.dataPath?.let { "/Dungeons/Content".plus(it.removePrefix("/Game")) }
            ?: throw RuntimeException("unknown enchantment id!")

        val indexes = DungeonsPakRegistry.index.filter { it.startsWith(imagePath) }
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

        return if (pakPath != null) DungeonsTextures.get(cacheKey, preprocess) { pakPath } else null
    }

    companion object: ResourceReadable() {
        val CommonNonGlidedInvestedPoints = listOf(1, 2, 3)
        val PowerfulNonGlidedInvestedPoints = listOf(2, 3, 4)
        val CommonGlidedInvestedPoints = listOf(2, 3, 4)
        val PowerfulGlidedInvestedPoints = listOf(3, 4, 5)

        private val PowerfulEnchantments =
            Json.decodeFromString<List<String>>(resourceText("databases/enchantments_powerful.json")).toSet()

        private val ArmorEnchantments =
            Json.decodeFromString<List<String>>(resourceText("databases/enchantments_armor.json")).toSet()

        private val MeleeEnchantments =
            Json.decodeFromString<List<String>>(resourceText("databases/enchantments_melee.json")).toSet()

        private val RangedEnchantments =
            Json.decodeFromString<List<String>>(resourceText("databases/enchantments_ranged.json")).toSet()

        private val ExclusiveEnchantments: Map<String, List<String>> =
            Json.decodeFromString(resourceText("databases/enchantments_exclusive.json"))

        private val StackableEnchantments =
            Json.decodeFromString<List<String>>(resourceText("databases/enchantments_stackable.json")).toSet()

        private val EnchantmentSpecialDescValue: Map<String, List<String>> =
            Json.decodeFromString(resourceText("databases/enchantments_special_descriptions.json"))
    }

}

@Serializable
@Immutable
data class ArmorPropertyData(
    val id: String,
    val defaultIn: List<String> = emptyList()
) {
    val description get() =
        (DungeonsLocalizations["ArmorProperties/${DungeonsLocalizations.Corrections.ArmorProperty[id] ?: id}_description"])
            ?.replace("{0}", "")
            ?.replace("{1}", "")
            ?.replace("{2}", "")
            ?.replace("개의", "추가")
            ?.replace("만큼", "")
            ?.replace("  ", " ")
            ?.trim()
}
