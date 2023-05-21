import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Database (
    val armorProperties: Set<String>,
    val enchantments: Set<EnchantmentData>,
    val gears: Set<Gear>,
    val artifacts: Set<Artifact>
) {

    fun findGear(id: String) = gears.find { it.id == id }

    fun findArtifact(id: String) = artifacts.find { it.id == id }

    fun findEnchantment(id: String) = enchantments.find { it.id == id }

    companion object {
        private val _current = load()
        val current get() = _current!!

        private fun load(): Database? {
            return try {
                val resource = {}::class.java.getResource("database.json")?.readText()
                    ?: throw RuntimeException("No Database resource Found!")
                Json.decodeFromString<Database>(resource)
            } catch (e: Exception) {
                null
            }
        }
    }

}

@Serializable
data class Gear(
    val id: String,
    val dataPath: String,
    val type: String
)

@Serializable
data class Artifact(
    val id: String,
    val dataPath: String
)

@Serializable
data class EnchantmentData(
    val id: String,
    val dataPath: String,
    val powerful: Boolean = false,
    val multipleAllowed: Boolean? = false,
    val applyFor: Set<String>? = null,
    val applyExclusive: List<String>? = null,
    val specialDescValues: List<String>? = null,
    val specialEffectText: String? = null
) {
    companion object {
        val CommonNonGlidedInvestedPoints = listOf(1, 2, 3)
        val PowerfulNonGlidedInvestedPoints = listOf(2, 3, 4)
        val CommonGlidedInvestedPoints = listOf(2, 3, 4)
        val PowerfulGlidedInvestedPoints = listOf(3, 4, 5)
    }
}
