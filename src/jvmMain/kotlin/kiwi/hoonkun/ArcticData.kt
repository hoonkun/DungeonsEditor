package kiwi.hoonkun

import androidx.compose.runtime.*
import kiwi.hoonkun.resources.Localizations
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import minecraft.dungeons.io.DungeonsSaveFile
import java.io.File

object ArcticSave {
    private val current = getOrCreate()

    var locale by mutableStateOf(current.locale)
    val recentFiles = current.recentFiles.toMutableStateList()
    var customPakLocation by mutableStateOf(current.customPakLocation)

    val recentSummaries by derivedStateOf {
        recentFiles
            .mapNotNull { path ->
                try { DungeonsSaveFile(path).summary() }
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
        localDataFile().writeText(
            Json.encodeToString(
                serializer = SerializableArcticSaveFile.serializer(),
                value = SerializableArcticSaveFile(locale, recentFiles, customPakLocation)
            )
        )
    }

    private fun localDataFile() = File("${System.getProperty("user.home")}/.dungeons_editor/saved_local.json")

    private fun getOrCreate(): SerializableArcticSaveFile {
        val local = localDataFile()
        if (!local.exists()) {
            local.parentFile.mkdirs()
            local.createNewFile()
            local.writeText("{}")
        }
        val raw = Json.decodeFromString(SerializableArcticSaveFile.serializer(), local.readText())
        return SerializableArcticSaveFile(
            locale = if (Localizations.supported.contains(raw.locale)) raw.locale else "en",
            recentFiles = raw.recentFiles.filter { File(it).exists() }.let { it.slice(0 until 4.coerceAtMost(it.size)) },
            customPakLocation = raw.customPakLocation
        )
    }
}


@Serializable
private data class SerializableArcticSaveFile(
    val locale: String = "en",
    val recentFiles: List<String> = emptyList(),
    val customPakLocation: String? = null
)

@Immutable
object ArcticSettings {
    val globalScale: Float

    val preloadTextures: Boolean

    init {
        val pwd = System.getenv("APPIMAGE")?.let { it.dropLast(it.length - it.lastIndexOf('/')) } ?: "."
        val file = File("$pwd/settings.arctic")
        val settings =
            if (file.exists())
                file
                    .readText()
                    .trim()
                    .split("\n")
                    .associate { it.split("=").let { segments -> segments[0] to segments[1] } }
            else
                emptyMap()

        globalScale = settings["scale"]?.toFloat()?.coerceIn(0.4f..1.35f) ?: 0.7f
        preloadTextures = settings["preload_textures"].let { it == "true" || it == null }
    }
}
