import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Database (
    val armorProperties: Set<String>,
    val enchantments: Map<String, String>,
    val gears: Map<String, List<String>>,
    val artifacts: Map<String, String>
) {

    companion object {
        val current = load()

        private fun load(): Database {
            val resource = {}::class.java.getResource("database.json")?.readText() ?: throw RuntimeException("No Database resource Found!")
            return Json.decodeFromString<Database>(resource)
        }
    }

}
