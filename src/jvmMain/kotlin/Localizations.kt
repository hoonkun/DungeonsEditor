import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import states.Enchantment

@Stable
class Localizations {

    companion object {
        val currentLocale by mutableStateOf("ko-KR")

        private val texts: Map<String, Map<String, String>>

        operator fun get(key: String): String? {
            return this.texts[currentLocale]?.get(key)
        }

        private val ItemNameCorrections = mapOf(
            "Powerbow" to "PowerBow",
            "Powerbow_Unique1" to "PowerBow_Unique1",
            "Powerbow_Unique2" to "PowerBow_Unique2",
            "TwistingVineBow_UNique1" to "TwistingVineBow_Unique1"
        )

        fun ItemName(type: String) = Localizations["ItemType/${ItemNameCorrections[type] ?: type}"] ?: "알 수 없는 아이템"
        fun ItemDescription(type: String) = Localizations["ItemType/Flavour_${ItemNameCorrections[type] ?: type}"]
        fun ItemFlavour(type: String) = Localizations["ItemType/Desc_${ItemNameCorrections[type] ?: type}"]

        private val EnchantmentNameCorrections = mapOf(
            "VoidTouchedMelee" to "VoidStrikeMelee",
            "VoidTouchedRanged" to "VoidStrikeRanged",
            "CriticalHit" to "Critical"
        )

        fun EnchantmentName(enchantment: Enchantment) =
            Localizations["Enchantment/${EnchantmentNameCorrections[enchantment.id] ?: enchantment.id}"] ?: "???"
        fun EnchantmentDescription(enchantment: Enchantment) =
            Localizations["Enchantment/${EnchantmentNameCorrections[enchantment.id] ?: enchantment.id}_desc"]
                ?.let { text ->
                    val enchantmentData = Database.current.enchantments.find { it.id == enchantment.id } ?: return@let null
                    var result = text
                    enchantmentData.specialDescValues?.forEachIndexed { index, value -> result = result.replace("{${index}}", Localizations[value] ?: "") }
                    result
                }

        fun EnchantmentEffect(enchantment: Enchantment) =
            Localizations[enchantment.data?.specialEffectText ?: "Enchantment/${EnchantmentNameCorrections[enchantment.id] ?: enchantment.id}_effect"]

        init {
            val newTexts = mutableMapOf<String, Map<String, String>>()
            val languages = listOf("ko-KR", "en-US")
            languages.forEach {
                newTexts[it] = Json.decodeFromString<Map<String, String>>({}::class.java.getResource("localization_ko-KR.json")!!.readText())
            }
            texts = newTexts
        }
    }

}