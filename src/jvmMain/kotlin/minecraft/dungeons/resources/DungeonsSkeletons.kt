package minecraft.dungeons.resources

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.utils.resourceText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import minecraft.dungeons.io.DungeonsPakRegistry
import minecraft.dungeons.values.DungeonsItem
import pak.PakIndex
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString


object DungeonsSkeletons {

    @Serializable
    @Immutable
    data class Item(
        val type: String,
        val dataPath: String,
        val variant: DungeonsItem.Variant
    ): Loadable {

        val unique = variant != DungeonsItem.Variant.Artifact && UniqueSuffixes.any { type.contains(it) }
        val limited = LimitedSuffixes.any { type.contains(it) }

        val builtInProperties = ArmorProperty[Unit].filter { it.defaultIn.contains(type) }

        val name get() =
            DungeonsLocalizations[DungeonsLocalizations.Corrections.ItemName[type] ?: "ItemType/${type}"]
                ?: Localizations["Unknown_item"]

        val flavour get() =
            DungeonsLocalizations["ItemType/Flavour_${DungeonsLocalizations.Corrections.ItemFlavour[type] ?: type}"]

        val description get() =
            DungeonsLocalizations["ItemType/Desc_${DungeonsLocalizations.Corrections.ItemDescription[type] ?: type}"]

        val inventoryIcon: ImageBitmap by lazy {
            retrieveImage("Inventory") { it.endsWith("_icon_inventory") }
        }

        val largeIcon: ImageBitmap by lazy {
            retrieveImage("Large", fallback = { it.endsWith("_icon_inventory") }) { it.endsWith("_icon") }
        }

        val appliedExclusiveEnchantments by lazy {
            ExclusiveEnchantments.entries
                .firstOrNull { it.value.contains(type) }
                ?.key
                ?.let { DungeonsSkeletons.Enchantment[it] }
        }

        override fun load() {
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

            if (fallback == null)
                throw RuntimeException("no image resource found with item $type, which type is $key")

            val candidate2 = indexes.find { fallback(it.lowercase().replaceAfterLast('.', "").removeSuffix(".")) }
            if (candidate2 != null)
                return DungeonsTextures.get(cacheKey) { candidate2 }

            throw RuntimeException("no image resource found with item $type, which type is $key")
        }

        companion object: Element {
            operator fun get(unused: Unit) = unused.let { DataHolder.items }
            operator fun get(key: String) = DataHolder.items.find { it.type == key }

            val UniqueSuffixes = listOf("_Unique", "_Spooky", "_Winter", "_Year")
            val LimitedSuffixes = listOf("_Spooky", "_Winter", "_Year")

            private val ExclusiveEnchantments: Map<String, List<String>> =
                Json.decodeFromString(resourceText("databases/enchantments_exclusive.json"))
        }
    }

    @Serializable
    @Immutable
    data class Enchantment(
        val id: String,
        val dataPath: String,
    ): Loadable {

        val powerful = PowerfulEnchantments.contains(id)
        val stackable = StackableEnchantments.contains(id)
        val applyFor = mutableSetOf<DungeonsItem.Variant>()
            .apply {
                if (ArmorEnchantments.contains(id)) add(DungeonsItem.Variant.Armor)
                if (MeleeEnchantments.contains(id)) add(DungeonsItem.Variant.Melee)
                if (RangedEnchantments.contains(id)) add(DungeonsItem.Variant.Ranged)
                // if (ExclusiveEnchantments.containsKey(id)) add("Exclusive") // ?
            }
            .toSet()

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

        override fun load() {
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
                val ps = sourcePixels[i].toUInt()
                val pb = backdropPixels[i].toUInt()

                // Source: ShinePattern
                val cs: (shift: Int) -> Float = {
                    // (((Ps and (0xffu shl (it * 8))) shr (it * 8)).toFloat() / 0xff).coerceIn(0f, 1f)
                    1f
                }
                val alphaS = (((ps and mask and 0x00ffffffu) shr (channel * 2 * 4)).toFloat() / 0xff).coerceIn(0f, 1f)

                // Backdrop: RealImage
                val cb: (shift: Int) -> Float = { (((pb and (0xffu shl (it * 8))) shr (it * 8)).toFloat() / 0xff).coerceIn(0f, 1f) }
                val alphaB = cb(3) // 1f

                // PorterDuff Values
                val fa = alphaB
                val fb = 1 - alphaS

                // Blending Functions
                val multiply = { b: Float, s: Float -> b * s }
                val screen = { b: Float, s: Float -> b + s - (b * s) }
                val hardlight = { b: Float, s: Float -> if (s <= 0.5f) multiply(b, 2 * s) else screen(b, 2 * s - 1) }
                val overlay = { b: Float, s: Float -> hardlight(s, b) }

                // Implementation
                val co: (shift: Int) -> Float = { alphaS * fa * ((1 - alphaB) * cs(it) + alphaB * overlay(cb(it), cs(it))) + alphaB * fb * cb(it) }
                val ao = alphaS * alphaB // As

                val toUIntChannel: Float.() -> UInt = { times(0xff).toInt().coerceIn(0, 0xff).toUInt() }

                val r = co(2).toUIntChannel()
                val g = co(1).toUIntChannel()
                val b = co(0).toUIntChannel()
                val a = ao.toUIntChannel()

                sourcePixels[i] = ((a shl 24) or (r shl 16) or (g shl 8) or b).toInt()
            }
            new.setRGB(0, 0, source.width, source.height, sourcePixels, 0, source.width)
            return new.toComposeImageBitmap()
        }

        private fun retrieveImage(cacheKey: String, criteria: (String) -> Boolean): ImageBitmap? {
            if (id == "Unset") { return DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png"] }

            val cached = DungeonsTextures.cached(cacheKey)
            if (cached != null) return cached

            val imagePath = Enchantment[id]?.dataPath?.let { "/Dungeons/Content".plus(it.removePrefix("/Game")) }
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

        companion object: Element {
            operator fun get(unused: Unit) = unused.let { DataHolder.enchantments }
            operator fun get(key: String) = DataHolder.enchantments.find { it.id == key }

            private val PowerfulEnchantments =
                Json.decodeFromString<List<String>>(resourceText("databases/enchantments_powerful.json")).toSet()

            private val ArmorEnchantments =
                Json.decodeFromString<List<String>>(resourceText("databases/enchantments_armor.json")).toSet()

            private val MeleeEnchantments =
                Json.decodeFromString<List<String>>(resourceText("databases/enchantments_melee.json")).toSet()

            private val RangedEnchantments =
                Json.decodeFromString<List<String>>(resourceText("databases/enchantments_ranged.json")).toSet()

            private val StackableEnchantments =
                Json.decodeFromString<List<String>>(resourceText("databases/enchantments_stackable.json")).toSet()

            private val EnchantmentSpecialDescValue: Map<String, List<String>> =
                Json.decodeFromString(resourceText("databases/enchantments_special_descriptions.json"))

            private fun String?.replaceFormatStrings() = this?.replace("{0}", "N")
        }
    }

    @Serializable
    @Immutable
    data class ArmorProperty(
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

        companion object: Element {
            operator fun get(unused: Unit) = unused.let { DataHolder.armorProperties }
            operator fun get(key: String) = DataHolder.armorProperties.find { it.id == key }
        }
    }

    object Initializer {

        private val KeyMappings: Map<String, String> =
            Json.decodeFromString(resourceText("databases/key_mappings.json"))

        private val ExcludedItems =
            Json.decodeFromString<List<String>>(resourceText("databases/excluded_items.json")).toSet()

        fun run(index: PakIndex) {
            DataHolder.armorProperties = Json.decodeFromString<Map<String, List<String>>>(resourceText("databases/armor_properties.json"))
                .map { (k, v) -> ArmorProperty(k, v) }
                .toSet()

            val items = mutableSetOf<Item>()
            val enchantments = mutableSetOf(Enchantment(id = "Unset", ""))

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

                val correctedName = KeyMappings[path.parent.name] ?: path.parent.name

                if (pathString.contains("Enchantments") && path.nameWithoutExtension.endsWith("_icon", true)) {
                    val enchantment = Enchantment(correctedName, dataPath)
                    if (enchantments.contains(enchantment)) return@forEach

                    enchantments.add(enchantment)
                }

                if (path.nameWithoutExtension.endsWith("_icon_inventory", true)) {
                    if (ExcludedItems.any { path.parent.name == it }) return@forEach

                    val type =
                        if (dataPath.contains("MeleeWeapons")) DungeonsItem.Variant.Melee
                        else if (dataPath.contains("RangedWeapons")) DungeonsItem.Variant.Ranged
                        else if (dataPath.contains("Armor")) DungeonsItem.Variant.Armor
                        else if (pathString.contains("Items")) DungeonsItem.Variant.Artifact
                        else return@forEach

                    val item = Item(correctedName, dataPath, type)
                    items.removeIf { it.type == item.type }
                    items.add(item)
                }

            }

            DataHolder.items = items
            DataHolder.enchantments = enchantments
        }

    }

    private object DataHolder {
        var armorProperties: Set<ArmorProperty> = emptySet()
        var enchantments: Set<Enchantment> = emptySet()
        var items: Set<Item> = emptySet()
    }

    interface Element

    interface Loadable {
        fun load()
    }

    class NotFoundException(key: String, type: Element):
        Exception("No skeleton found: ${type::class.simpleName?.replace("Skeleton", "")}[$key]")
}
