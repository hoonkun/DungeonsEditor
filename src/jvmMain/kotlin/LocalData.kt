import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.toMutableStateList
import dungeons.readDungeonsSummary
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

        val recentSummaries by derivedStateOf {
            recentFiles
                .mapNotNull { path ->
                    try { File(path).readDungeonsSummary() }
                    catch (e: Exception) { null }
                }
        }

        fun updateRecentFiles(path: String) {
            if (!recentFiles.contains(path)) recentFiles.add(0, path)
            else recentFiles.add(0, recentFiles.removeAt(recentFiles.indexOf(path)))

            if (recentFiles.size > 4) recentFiles.removeRange(4, recentFiles.size)

            save()
        }

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