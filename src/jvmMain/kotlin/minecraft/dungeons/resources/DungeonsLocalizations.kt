package minecraft.dungeons.resources

import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.utils.ResourceReadable
import kotlinx.serialization.json.Json
import minecraft.dungeons.io.DungeonsPakRegistry
import utils.LocalizationResource

object DungeonsLocalizations: ResourceReadable() {
    private var _texts: Map<String, Map<String, String>>? = null
    private val texts get() = _texts!!

    operator fun get(key: String): String? {
        return texts[ArcticSettings.locale]?.get(key)
    }

    object Corrections {
        private val ItemCorrections =
            Json.decodeFromString<Map<String, String>>(resourceText("localizations/corrections_item_base.json"))

        val ItemName =
            ItemCorrections + Json.decodeFromString<Map<String, String>>(resourceText("localizations/corrections_item_name.json"))

        val ItemFlavour =
            ItemCorrections + Json.decodeFromString<Map<String, String>>(resourceText("localizations/corrections_item_flavour.json"))

        val ItemDescription =
            ItemCorrections + Json.decodeFromString<Map<String, String>>(resourceText("localizations/corrections_item_description.json"))

        val ArmorProperty =
            Json.decodeFromString<Map<String, String>>(resourceText("localizations/corrections_armor_properties.json"))

        private val Enchantment =
            Json.decodeFromString<Map<String, String>>(resourceText("localizations/corrections_enchantment_base.json"))

        val EnchantmentName =
            Enchantment + Json.decodeFromString<Map<String, String>>(resourceText("localizations/corrections_enchantment_name.json"))

        val EnchantmentDescription =
            Enchantment + Json.decodeFromString<Map<String, String>>(resourceText("localizations/corrections_enchantment_description.json"))

        val EnchantmentFixedEffect =
            Json.decodeFromString<Map<String, String>>(resourceText("localizations/corrections_enchantment_shared_label.json"))

        val EnchantmentEffect =
            Enchantment + Json.decodeFromString<Map<String, String>>(resourceText("localizations/corrections_enchantment_effect.json"))
    }

    private fun locresPath(locale: String): String =
        "/Dungeons/Content/Localization/Game/$locale/Game.locres"

    fun initialize() {
        val newTexts = mutableMapOf<String, Map<String, String>>()
        val languages = listOf("ko-KR", "en")
        languages.forEach {
            newTexts[it] = LocalizationResource.read(DungeonsPakRegistry.index.getFileBytes(locresPath(it))!!)
        }
        _texts = newTexts
    }

}