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

        val ItemNameCorrections = mapOf(
            "Powerbow" to "PowerBow",
            "Powerbow_Unique1" to "PowerBow_Unique1",
            "Powerbow_Unique2" to "PowerBow_Unique2",
            "TwistingVineBow_UNique1" to "TwistingVineBow_Unique1"
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