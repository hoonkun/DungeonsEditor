import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Stable
class Localizations {

    companion object {
        val currentLocale by mutableStateOf("ko-KR")

        private val texts: Map<String, Map<String, String>>

        operator fun get(key: String): String? {
            return this.texts[currentLocale]?.get(key)
        }

        private val ItemCorrections = mapOf<String, String>()

        private val ItemNameCorrections = mapOf(
            "Powerbow" to "PowerBow",
            "Powerbow_Unique1" to "PowerBow_Unique1",
            "Powerbow_Unique2" to "PowerBow_Unique2"
        )

        private val ItemFlavourCorrections = ItemCorrections + mapOf()
        private val ItemDescriptionCorrections = ItemCorrections + mapOf()

        fun ItemName(type: String) = Localizations["ItemType/${ItemNameCorrections[type] ?: type}"] ?: "알 수 없는 아이템"
        fun ItemDescription(type: String) = Localizations["ItemType/Desc_${ItemDescriptionCorrections[type] ?: type}"]
        fun ItemFlavour(type: String) = Localizations["ItemType/Flavour_${ItemFlavourCorrections[type] ?: type}"]

        private val EnchantmentCorrections = mapOf(
            "VoidTouchedMelee" to "VoidStrikeMelee",
            "VoidTouchedRanged" to "VoidStrikeRanged",
            "CriticalHit" to "Critical",
            "EnigmaResonatorMelee" to "EnigmaMelee",
            "EnigmaResonatorRanged" to "EnigmaRanged",
            "FireAspect" to "Fire",
            "AnimaConduitMelee" to "Anima",
            "AnimaConduitRanged" to "AnimaRanged",
            "PoisonedMelee" to "Poisoned",
            "PoisonedRanged" to "Poisoned",
            "SoulSiphon" to "Soul",
            "Shockwave" to "Shock",
            "Gravity" to "GravityRanged",
            "MultiShot" to "Multi",
            "TempoTheft" to "Tempo"
        )

        private val EnchantmentNameCorrections = EnchantmentCorrections + mapOf(
            "Deflecting" to "Deflect",
            "Celerity" to "Cool Down",
            "AnimaConduitRanged" to "Anima",
            "Accelerating" to "Accelerate",
            "EnigmaMelee" to "Enigma",
            "EnigmaRanged" to "Enigma"
        )

        private val EnchantmentDescriptionCorrections = EnchantmentCorrections + mapOf(
            "ChainReaction" to "Chain",
            "EmeraldDivination" to "EmeraldDivination_effect",
            "Flee" to "Flee_effect",
            "DeathBarter" to "DeathBarter_effect",
            "Reckless" to "ShardArmor"
        )
        // Artifact Charge CHECK??
        private val EnchantmentFixedEffectCorrections = mapOf(
            "CriticalHit" to "Enchantment/label_chanceToTrigger",
            "PotionFortification" to "Enchantment/label_duration",
            "Rampaging" to "Enchantment/label_duration",
            "PoisonedMelee" to "Enchantment/label_damagePerSecond",
            "PoisonedRanged" to "Enchantment/label_damagePerSecond",
            "Stunning" to "Enchantment/label_chanceToTrigger",
            "Chains" to "Enchantment/label_duration",
            "MultiShot" to "Enchantment/label_chanceToTrigger",
            "Infinity" to "Enchantment/label_chanceToTrigger",
            "ChainReaction" to "Enchantment/label_chanceToTrigger",
            "WildRage" to "Enchantment/label_chanceToTrigger",
            "Ricochet" to "Enchantment/label_chanceToTrigger",
            "SurpriseGift" to "Enchantment/label_chanceToTrigger",
            "Deflecting" to "Enchantment/label_chanceToTrigger",
            "SpeedSynergy" to "Enchantment/label_duration",
            "SpiritSpeed" to "Enchantment/label_duration"
        )
        private val EnchantmentEffectCorrections = EnchantmentCorrections + mapOf(
            "Gravity" to "GravityPulse",
            "GravityMelee" to "GravityPulse",
            "FireAspect" to "FireTrail",
            "ShadowFlash" to "shadowflash",
            "ShadowFeast" to "shadowfeast",
            "BagOfSouls" to "BagOfSoul"
        )

        fun EnchantmentName(enchantment: EnchantmentData) =
            Localizations["Enchantment/${EnchantmentNameCorrections[enchantment.id] ?: enchantment.id}"] ?: "???"
        fun EnchantmentDescription(enchantment: EnchantmentData) =
            if (enchantment.id == "Unset")
                "이 슬롯을 비활성화 상태로 변경합니다.\n'화려한'에 설정된 효과 부여의 경우 금박이 지워진 상태로 변경됩니다."
            else
                Localizations["Enchantment/${EnchantmentDescriptionCorrections[enchantment.id] ?: enchantment.id}_desc"]
                    ?.let { text ->
                        val enchantmentData = Database.current.enchantments.find { it.id == enchantment.id } ?: return@let null
                        var result = text
                        enchantmentData.specialDescValues?.forEachIndexed { index, value -> result = result.replace("{${index}}", Localizations[value] ?: "") }
                        result
                    }

        fun EnchantmentEffect(enchantment: EnchantmentData) =
            if (enchantment.id == "Unset") "{0} 확률로 아무것도 하지 않습니다?"
            else Localizations[EnchantmentFixedEffectCorrections[enchantment.id] ?: "Enchantment/${EnchantmentEffectCorrections[enchantment.id] ?: enchantment.id}_effect"]

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