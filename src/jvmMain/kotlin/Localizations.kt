import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Stable
class Localizations {

    companion object {
        var currentLocale by mutableStateOf("ko-KR")

        private val texts: Map<String, Map<String, String>>

        operator fun get(key: String): String? {
            return this.texts[currentLocale]?.get(key)
        }

        private val ItemCorrections = mapOf<String, String>()

        val ItemNameCorrections = ItemCorrections + mapOf(
            "Powerbow" to "PowerBow",
            "Powerbow_Unique1" to "PowerBow_Unique1",
            "Powerbow_Unique2" to "PowerBow_Unique2"
        )

        val ItemFlavourCorrections = ItemCorrections + mapOf()
        val ItemDescriptionCorrections = ItemCorrections + mapOf()

        val ArmorPropertyCorrections = mapOf(
            "ItemCooldownDecrease" to "ArtifactCooldownDecrease",
            "ItemDamageBoost" to "ArtifactDamageBoost",
            "SlowResistance" to "FreezingResistance"
        )

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

        val EnchantmentNameCorrections = EnchantmentCorrections + mapOf(
            "Deflecting" to "Deflect",
            "Celerity" to "Cool Down",
            "AnimaConduitRanged" to "Anima",
            "Accelerating" to "Accelerate",
            "EnigmaMelee" to "Enigma",
            "EnigmaRanged" to "Enigma"
        )

        val EnchantmentDescriptionCorrections = EnchantmentCorrections + mapOf(
            "ChainReaction" to "Chain",
            "EmeraldDivination" to "EmeraldDivination_effect",
            "Flee" to "Flee_effect",
            "DeathBarter" to "DeathBarter_effect",
            "Reckless" to "ShardArmor"
        )

        val EnchantmentFixedEffectCorrections = mapOf(
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
        val EnchantmentEffectCorrections = EnchantmentCorrections + mapOf(
            "Gravity" to "GravityPulse",
            "GravityMelee" to "GravityPulse",
            "FireAspect" to "FireTrail",
            "ShadowFlash" to "shadowflash",
            "ShadowFeast" to "shadowfeast",
            "BagOfSouls" to "BagOfSoul"
        )

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