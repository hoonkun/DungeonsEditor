import androidx.compose.ui.graphics.ImageBitmap
import extensions.GameResources
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

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
    val multipleAllowed: Boolean = false,
    val applyFor: Set<String>? = null,
    val applyExclusive: List<String>? = null,
    val specialDescValues: List<String>? = null
) {

    fun ImageScale(): Float =
        if (id == "Unset") 1.05f else 1.425f

    fun Image(): ImageBitmap =
        InternalImage { it.name.lowercase().endsWith("_icon.png") && !it.name.lowercase().endsWith("shine_icon.png") }

    fun ShineImage(): ImageBitmap =
        InternalImage { it.name.lowercase().endsWith("shine_icon.png") }

    private fun InternalImage(criteria: (File) -> Boolean): ImageBitmap {
        if (id == "Unset") return GameResources.image("EnchantmentUnset") { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png" }

        val cached = GameResources.image(id)
        if (cached != null) return cached

        val imagePath = Database.current.findEnchantment(id)?.dataPath ?: throw RuntimeException("unknown enchantment id!")
        val dataDirectory = File("${Constants.GameDataDirectoryPath}${imagePath}")
        val imageFile = dataDirectory.listFiles().let { files ->
            files?.find { it.extension == "png" && criteria(it) }
        } ?: throw RuntimeException("no image resource found: {$id}!")

        return GameResources.image(id, false) { imageFile.absolutePath }
    }

    companion object {
        val CommonNonGlidedInvestedPoints = listOf(1, 2, 3)
        val PowerfulNonGlidedInvestedPoints = listOf(2, 3, 4)
        val CommonGlidedInvestedPoints = listOf(2, 3, 4)
        val PowerfulGlidedInvestedPoints = listOf(3, 4, 5)
    }
}
