import androidx.compose.runtime.*
import dungeons.Localizations
import dungeons.readDungeonsSummary
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class LocalDataRaw(
    val locale: String = "en",
    val recentFiles: List<String> = emptyList(),
    val customPakLocation: String? = null
)

class LocalData {
    companion object {
        private var current = getOrCreateLocalData()

        var locale by mutableStateOf(current.locale)

        val recentFiles = current.recentFiles.toMutableStateList()

        var customPakLocation by mutableStateOf(current.customPakLocation)

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
            localDataFile().writeText(Json.encodeToString(LocalDataRaw.serializer(), LocalDataRaw(locale, recentFiles, customPakLocation)))
        }
    }
}

private fun localDataFile() = File("${System.getProperty("user.home")}/.dungeons_editor/saved_local.json")

private fun getOrCreateLocalData(): LocalDataRaw {
    val local = localDataFile()
    if (!local.exists()) {
        local.parentFile.mkdirs()
        local.createNewFile()
        local.writeText("{}")
    }
    val raw = Json.decodeFromString(LocalDataRaw.serializer(), local.readText())
    return LocalDataRaw(
        locale = if (Localizations.supported.contains(raw.locale)) raw.locale else "en",
        recentFiles = raw.recentFiles.filter { File(it).exists() }.let { it.slice(0 until 4.coerceAtMost(it.size)) },
        customPakLocation = raw.customPakLocation
    )
}