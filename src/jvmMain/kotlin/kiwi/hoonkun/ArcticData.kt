package kiwi.hoonkun

import androidx.compose.runtime.*
import kiwi.hoonkun.resources.Localizations
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import minecraft.dungeons.io.DungeonsJsonFile
import java.io.File

object ArcticSettings {
    private val json = Json { isLenient = true }

    private val current = getOrCreate()

    var globalScale: Float by mutableStateOf(current.scale)
    var preloadTextures: Boolean by mutableStateOf(current.preloadTextures)

    var locale by mutableStateOf(current.locale)
    var customPakLocation by mutableStateOf(current.customPakLocation)

    private val recentFiles = current.recentFiles.toMutableStateList()

    val recentSummaries by derivedStateOf {
        recentFiles
            .mapNotNull { path ->
                try { DungeonsJsonFile(path).summary() }
                catch (e: Exception) { null }
            }
    }

    fun updateRecentFiles(path: String) {
        if (!recentFiles.contains(path)) recentFiles.add(0, path)
        else recentFiles.add(0, recentFiles.removeAt(recentFiles.indexOf(path)))

        if (recentFiles.size > 4) recentFiles.removeRange(4, recentFiles.size)

        save()
    }

    fun withSave(block: ArcticSettings.() -> Unit) {
        block(this)
        save()
    }

    fun save() {
        localDataFile().writeText(
            Json.encodeToString(
                serializer = SerializableArcticSettings.serializer(),
                value = SerializableArcticSettings(
                    locale,
                    recentFiles,
                    customPakLocation,
                    preloadTextures,
                    globalScale,
                )
            )
        )
    }

    private fun localDataFile() = File("${System.getProperty("user.home")}/.dungeons_editor/saved_local.json")

    private fun getOrCreate(): SerializableArcticSettings {
        val local = localDataFile()
        if (!local.exists()) {
            local.parentFile.mkdirs()
            local.createNewFile()
            local.writeText("{}")
        }
        val raw = json.decodeFromString(SerializableArcticSettings.serializer(), local.readText())
        return SerializableArcticSettings(
            locale = if (Localizations.supported.contains(raw.locale)) raw.locale else "en",
            recentFiles = raw.recentFiles.filter { File(it).exists() }.let { it.slice(0 until 4.coerceAtMost(it.size)) },
            customPakLocation = raw.customPakLocation,
            scale = raw.scale.coerceIn(0.4f..1.35f),
            preloadTextures = raw.preloadTextures
        )
    }
}


@Serializable
private data class SerializableArcticSettings(
    val locale: String = "en",
    val recentFiles: List<String> = emptyList(),
    val customPakLocation: String? = null,
    val preloadTextures: Boolean = true,
    val scale: Float = 0.55f
)
