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
            val enchantments = mutableSetOf(EnchantmentData(id = "Unset", ""))

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
        ?: Localizations.UiText("Unknown_item")
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
        else DungeonsLocalizations["Enchantment/${DungeonsLocalizations.Corrections.EnchantmentDescription[id] ?: id}_desc"].replaceFormatStrings()

    val effect: String? get() =
        if (id == "Unset") Localizations["enchantment_unset_effect"]
        else (DungeonsLocalizations[DungeonsLocalizations.Corrections.EnchantmentFixedEffect[id] ?: "Enchantment/${DungeonsLocalizations.Corrections.EnchantmentEffect[id] ?: id}_effect"]).replaceFormatStrings()

    val icon: ImageBitmap by lazy {
        retrieveImage(id) { it.startsWith("t_") && it.endsWith("_icon") && !it.endsWith("shine_icon") }
            ?: throw RuntimeException("no image resource found: {$id}!")
    }

    private val shinePattern: ImageBitmap? by lazy {
        if (id == "Unset") null
        else retrieveImage("${id}_shine") { it.startsWith("t_") && it.endsWith("shine_icon") }
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
        val source = shinePattern?.toAwtImage() ?: return null
        val backdrop = icon.toAwtImage()

        val new = BufferedImage(source.width, source.height, backdrop.type)
        val sourcePixels = source.getRGB(0, 0, source.width, source.height, null, 0, source.width)
        val backdropPixels = backdrop.getRGB(0, 0, backdrop.width, backdrop.height, null, 0, source.width)
        val mask = 0xff000000u or (0xffu shl (channel * 2 * 4)) // 0xff0000ff or 0xff00ff00 or 0xffff0000
        for (i in sourcePixels.indices) {
            val filtered = sourcePixels[i].toUInt() and mask // deletes except specified channel and alpha
            if ((filtered and 0x00ffffffu) == 0u) {
                sourcePixels[i] = 0
                continue
            }

            // Apply BlendMode.Overlay here, to avoid real-time blend mode rendering in compose layer.
            // W3C Thank you again!!

            // Compositing Documentation
            //
            // co = Cs x αs + Cb x αb x (1 - αs) // ?
            //
            // co: the premultiplied pixel value after compositing
            // Cs: the color value of the source graphic element being composited
            // αs: the alpha value of the source graphic element being composited
            // Cb: the color value of the backdrop
            // αb: the alpha value of the backdrop
            //
            // αo = αs + αb x (1 - αs) ----------------------------------------------------------------------- (3)
            //
            // αo: the alpha value of the composite
            // αs: the alpha value of the graphic element being composited
            // αb: the alpha value of the backdrop
            //
            // Blending Documentation
            //
            // Cs = (1 - αb) x Cs + αb x B(Cb, Cs) -> update source color with blending function ------------- (1)
            // Co = αs x Fa x Cs + αb x Fb x Cb -> result color with alpha is applied ------------------------ (2)
            //
            // Cs: is the source color
            // Cb: is the backdrop color
            // αs: is the source alpha
            // αb: is the backdrop alpha
            // B(Cb, Cs): is the mixing function
            // Fa: is defined by the Porter Duff operator in use
            // Fb: is defined by the Porter Duff operator in use

            // What we use:
            //
            // Porter Duff
            //     Fa = αb; Fb = 0 -> this is Source In Porter Duff, which deletes all pixels excepts source.
            //
            // Blending Functions
            // Overlay
            //     B(Cb, Cs) = HardLight(Cs, Cb)
            // HardLight
            //     if(Cs <= 0.5)
            //         B(Cb, Cs) = Multiply(Cb, 2 x Cs)
            //     else
            //         B(Cb, Cs) = Screen(Cb, 2 x Cs -1)
            // Multiply
            //     B(Cb, Cs) = Cb x Cs
            // Screen
            //     B(Cb, Cs) = Cb + Cs - (Cb x Cs)

            // Raw pixel values
            val Ps = sourcePixels[i].toUInt()
            val Pb = backdropPixels[i].toUInt()

            // Source: ShinePattern
            val Cs: (shift: Int) -> Float = {
                // (((Ps and (0xffu shl (it * 8))) shr (it * 8)).toFloat() / 0xff).coerceIn(0f, 1f)
                1f
            }
            val As = (((Ps and mask and 0x00ffffffu) shr (channel * 2 * 4)).toFloat() / 0xff).coerceIn(0f, 1f)

            // Backdrop: RealImage
            val Cb: (shift: Int) -> Float = { (((Pb and (0xffu shl (it * 8))) shr (it * 8)).toFloat() / 0xff).coerceIn(0f, 1f) }
            val Ab = Cb(3) // 1f

            // PorterDuff Values
            val Fa = Ab
            val Fb = 1 - As

            // Blending Functions
            val Multiply: BlendingFunction = { Cb, Cs -> Cb * Cs }
            val Screen: BlendingFunction = { Cb, Cs -> Cb + Cs - (Cb * Cs) }
            val HardLight: BlendingFunction = { Cb, Cs -> if (Cs <= 0.5f) Multiply(Cb, 2 * Cs) else Screen(Cb, 2 * Cs - 1) }
            val Overlay: BlendingFunction = { Cb, Cs -> HardLight(Cs, Cb) }

            // Implementation
            val Co: (shift: Int) -> Float = { As * Fa * ((1 - Ab) * Cs(it) + Ab * Overlay(Cb(it), Cs(it))) + Ab * Fb * Cb(it) }
            val Ao = As * Ab // As

            val toUIntChannel: Float.() -> UInt = { times(0xff).toInt().coerceIn(0, 0xff).toUInt() }

            val r = Co(2).toUIntChannel()
            val g = Co(1).toUIntChannel()
            val b = Co(0).toUIntChannel()
            val a = Ao.toUIntChannel()

            sourcePixels[i] = ((a shl 24) or (r shl 16) or (g shl 8) or b).toInt()
        }
        new.setRGB(0, 0, source.width, source.height, sourcePixels, 0, source.width)
        return new.toComposeImageBitmap()
    }

    private fun retrieveImage(cacheKey: String, criteria: (String) -> Boolean): ImageBitmap? {
        if (id == "Unset") { return DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png"] }

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

        private fun String?.replaceFormatStrings() = this?.replace("{0}", "N")
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
            ?.replace("{0}", "N")
            ?.replace("{1}", "M")
            ?.replace("{2}", "K")
            ?.replace("  ", " ")
            ?.trim()
}

typealias BlendingFunction = (Float, Float) -> Float
