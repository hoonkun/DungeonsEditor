package minecraft.dungeons.resources

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.utils.nameWithoutExtension
import kiwi.hoonkun.utils.removeExtension
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
            DungeonsLocalizations[ItemName[type] ?: "ItemType/$type"] ?: Localizations["unknown_item"]

        val flavour get() =
            DungeonsLocalizations[ItemFlavour[type] ?: "ItemType/Flavour_$type"]

        val description get() =
            DungeonsLocalizations[ItemDescription[type] ?: "ItemType/Desc_$type"]

        val inventoryIcon: ImageBitmap by lazy {
            retrieveImage(
                key = "Inventory",
                { it.endsWith("_icon_inventory") }
            )
        }

        val largeIcon: ImageBitmap by lazy {
            retrieveImage(
                key = "Large",
                { it.endsWith("_icon") },
                { it.endsWith("_icon_inventory") }
            )
        }

        val appliedExclusiveEnchantment by lazy {
            ExclusiveEnchantments.entries
                .firstOrNull { it.value.contains(type) }
                ?.key
                ?.let { Enchantment[it] }
        }

        override fun load() {
            inventoryIcon
            largeIcon
        }

        private fun retrieveImage(
            key: String,
            vararg criteria: (String) -> Boolean
        ): ImageBitmap {
            val cacheKey = "$type-$key"
            val cached = DungeonsTextures.cached(cacheKey)
            if (cached != null) return cached

            val matches = criteria.firstNotNullOfOrNull { predicate ->
                DungeonsPakRegistry.index
                    .filter { it.startsWith(dataPath) }
                    .find { predicate(it.lowercase().removeExtension()) }
            }
                ?: throw RuntimeException("no image resource found with item $type, which type is $key")

            return DungeonsTextures[matches, cacheKey]
        }

        companion object Item: Element {
            operator fun get(unused: Unit): Set<DungeonsSkeletons.Item> = unused.let { DataHolder.items }
            operator fun get(key: String) = DataHolder.items.find { it.type == key }

            private val UniqueSuffixes = listOf("_Unique", "_Spooky", "_Winter", "_Year")
            private val LimitedSuffixes = listOf("_Spooky", "_Winter", "_Year")

            private val ExclusiveEnchantments: Map<String, List<String>> = Json.decodeDatabaseResource("enchantments_exclusive.json")

            private val ItemCorrections = Json.decodeLocResource("corrections_item_base.json")
            private val ItemName = ItemCorrections + Json.decodeLocResource("corrections_item_name.json")
            private val ItemFlavour = ItemCorrections + Json.decodeLocResource("corrections_item_flavour.json")
            private val ItemDescription = ItemCorrections + Json.decodeLocResource("corrections_item_description.json")
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
            }
            .toSet()

        val name: String get() =
            DungeonsLocalizations[EnchantmentName[id] ?: "Enchantment/$id"] ?: Localizations["unknown_enchantment"]

        val description: String? get() =
            DungeonsLocalizations[EnchantmentDescription[id] ?: "Enchantment/${id}_desc"].replaceFormatStrings()

        val effect: String? get() =
            DungeonsLocalizations[EnchantmentEffect[id] ?: "Enchantment/${id}_effect"].replaceFormatStrings()

        val icon: ImageBitmap by lazy {
            if (isNotValid())
                return@lazy DungeonsTextures["/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png"]

            retrieveImage(
                cacheKey = "$id-Icon",
                criteria = { it.startsWith("t_") && it.endsWith("_icon") && !it.endsWith("shine_icon") }
            )
                ?: throw RuntimeException("no image resource found: $id!")
        }

        private val shinePattern: ImageBitmap? by lazy {
            if (isNotValid())
                return@lazy null

            retrieveImage(
                cacheKey = "$id-ShinePattern",
                criteria = { it.startsWith("t_") && it.endsWith("shine_icon") }
            )
                ?.toAwtImage()
                ?.let {
                    BufferedImage(it.width, it.height, BufferedImage.TYPE_INT_RGB).apply {
                        createGraphics().apply {
                            color = Color.BLACK
                            fillRect(0, 0, width, height)
                            drawImage(it, 0, 0, null)
                        }
                            .dispose()
                    }
                }
                ?.toComposeImageBitmap()
        }

        val shinePatterns: ShinePatterns? by lazy {
            val r = filterShinePattern(2) ?: return@lazy null
            val g = filterShinePattern(1) ?: return@lazy null
            val b = filterShinePattern(0) ?: return@lazy null

            ShinePatterns(r, g, b)
        }

        override fun load() {
            icon
            shinePatterns
        }

        fun isValid() = id != "Unset"
        fun isNotValid() = id == "Unset"

        private fun retrieveImage(
            cacheKey: String,
            criteria: (String) -> Boolean
        ): ImageBitmap? {
            val cached = DungeonsTextures.cached(cacheKey)
            if (cached != null) return cached

            return DungeonsPakRegistry.index.filter { it.startsWith(dataPath) }
                .find { criteria(it.lowercase().nameWithoutExtension()) }
                ?.let { DungeonsTextures[it, cacheKey] }
        }

        private fun filterShinePattern(channel: Int): ImageBitmap? {
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

        @Immutable
        data class ShinePatterns(
            val r: ImageBitmap,
            val g: ImageBitmap,
            val b: ImageBitmap
        )

        companion object Enchantment: Element {
            operator fun get(unused: Unit): Set<DungeonsSkeletons.Enchantment> = unused.let { DataHolder.enchantments }
            operator fun get(key: String) = DataHolder.enchantments.find { it.id == key }

            private fun String?.replaceFormatStrings() = this?.replace("{0}", "N")

            private val PowerfulEnchantments: Set<String> = Json.decodeDatabaseResource("enchantments_powerful.json")
            private val ArmorEnchantments: Set<String> = Json.decodeDatabaseResource("enchantments_armor.json")
            private val MeleeEnchantments: Set<String> = Json.decodeDatabaseResource("enchantments_melee.json")
            private val RangedEnchantments: Set<String> = Json.decodeDatabaseResource("enchantments_ranged.json")
            private val StackableEnchantments: Set<String> = Json.decodeDatabaseResource("enchantments_stackable.json")

            private val EnchantmentCorrections = Json.decodeLocResource("corrections_enchantment_base.json")
            private val EnchantmentName = EnchantmentCorrections + Json.decodeLocResource("corrections_enchantment_name.json")
            private val EnchantmentDescription = EnchantmentCorrections + Json.decodeLocResource("corrections_enchantment_description.json")
            private val EnchantmentEffect = EnchantmentCorrections + Json.decodeLocResource("corrections_enchantment_effect.json")
        }
    }

    @Serializable
    @Immutable
    data class ArmorProperty(
        val id: String,
        val defaultIn: List<String> = emptyList()
    ) {
        val description get() =
            (DungeonsLocalizations["ArmorProperties/${ArmorPropertyCorrections[id] ?: id}_description"])
                ?.replace("{0}", "N")
                ?.replace("{1}", "M")
                ?.replace("{2}", "K")
                ?.replace("  ", " ")
                ?.trim()

        companion object ArmorProperty: Element {
            operator fun get(unused: Unit): Set<DungeonsSkeletons.ArmorProperty> = unused.let { DataHolder.armorProperties }
            operator fun get(key: String) = DataHolder.armorProperties.find { it.id == key }

            val ArmorPropertyCorrections = Json.decodeLocResource("corrections_armor_properties.json")
        }
    }

    object Initializer {

        private val KeyMappings: Map<String, String> =
            Json.decodeDatabaseResource("key_mappings.json")

        private val ExcludedItems: Set<String> =
            Json.decodeDatabaseResource("excluded_items.json")

        init {
            DataHolder.armorProperties = Json.decodeDatabaseResource<Map<String, List<String>>>("armor_properties.json")
                .map { (k, v) -> ArmorProperty(k, v) }
                .toMutableSet()
        }

        fun run(index: PakIndex) {
            DataHolder.items.clear()
            DataHolder.enchantments.clear()

            DataHolder.enchantments.add(Enchantment(id = "Unset", ""))

            index.toList()
                .mapNotNull {
                    val path = Path(it)

                    val isInvalid = it.contains("ArmorProperties") && !it.contains("Cues")
                    val isLevel = it.contains("data") && it.contains("levels")
                    val isNotTexture = !path.name.startsWith("T") || !it.lowercase().contains("_icon")

                    if (isInvalid || isLevel || isNotTexture) return@mapNotNull null
                    path
                }
                .forEach { path ->
                    val string = path.pathString
                    val name = path.nameWithoutExtension

                    val dataPath = path.parent.pathString.replace("\\", "/")

                    val correctedName = KeyMappings[path.parent.name] ?: path.parent.name

                    if (string.contains("Enchantments") && name.endsWith("_icon", ignoreCase = true)) {
                        if (DataHolder.enchantments.any { it.id == correctedName }) return@forEach

                        DataHolder.enchantments.add(Enchantment(correctedName, dataPath))
                    }

                    if (name.endsWith("_icon_inventory", ignoreCase = true)) run block@{
                        if (ExcludedItems.any { path.parent.name == it }) return@block
                        if (DataHolder.items.any { it.type == correctedName }) return@block

                        val type =
                            if (dataPath.contains("MeleeWeapons")) DungeonsItem.Variant.Melee
                            else if (dataPath.contains("RangedWeapons")) DungeonsItem.Variant.Ranged
                            else if (dataPath.contains("Armor")) DungeonsItem.Variant.Armor
                            else if (string.contains("Items")) DungeonsItem.Variant.Artifact
                            else return@forEach

                        DataHolder.items.add(Item(correctedName, dataPath, type))
                    }
                }
        }

    }

    private object DataHolder {
        var armorProperties = mutableSetOf<ArmorProperty>()
        var enchantments = mutableSetOf<Enchantment>()
        var items = mutableSetOf<Item>()
    }

    interface Element

    interface Loadable { fun load() }

    class NotFoundException(key: String, type: Element): Exception("No skeleton found: ${type::class.simpleName}[$key]")

    private inline fun <reified T>Json.decodeDatabaseResource(path: String, prefix: String = "databases/"): T =
        decodeFromString<T>(resourceText("$prefix$path"))

    private fun Json.decodeLocResource(path: String, prefix: String = "localizations/") =
        decodeFromString<Map<String, String>>(resourceText("$prefix$path"))

}
