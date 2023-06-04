import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class LocalData(
    val recentFiles: List<String> = emptyList()
) {
    companion object {

        private var current = getOrCreateLocalData()

        val recentFiles = current.recentFiles.toMutableStateList()

        fun save() {
            localDataFile().writeText(Json.encodeToString(LocalData(recentFiles)))
        }

    }
}

private fun localDataFile() = File("${System.getProperty("user.home")}/.dungeons_editor/saved_local.json")

private fun getOrCreateLocalData(): LocalData {
    val local = localDataFile()
    if (!local.exists()) {
        local.parentFile.mkdirs()
        local.createNewFile()
        local.writeText("{}")
    }
    val raw = Json.decodeFromString<LocalData>(local.readText())
    return LocalData(raw.recentFiles.filter { File(it).exists() })
}