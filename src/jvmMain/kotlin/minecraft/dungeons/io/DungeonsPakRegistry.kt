package minecraft.dungeons.io

import Keyset
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import minecraft.dungeons.resources.DungeonsDatabase
import minecraft.dungeons.resources.DungeonsTextures
import pak.PakIndex
import pak.PathPakFilter
import parsers.objects.fields.Guid
import java.io.File
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

object DungeonsPakRegistry {
    private var _index: PakIndex? by mutableStateOf(null)
    val index by derivedStateOf { _index!! }

    fun initialize(customLocation: String? = null): Boolean {
        if (_index != null) return true

        val pakPath = detectPakPath(customLocation) ?: return false

        val newIndex = PakIndex(
            File(pakPath).listFiles()!!.sortedBy { it.name },
            useCache = true,
            caseSensitive = true,
            filter = PathPakFilter(listOf("/Dungeons/Content"), false)
        )
        newIndex.useKey(Guid.Zero, Keyset.PakKey)

        _index = newIndex

        DungeonsDatabase.Initializer.run(newIndex)
        DungeonsTextures.Initializer.run(newIndex)

        return true
    }

    private fun detectPakPath(customPakLocation: String? = null): String? {
        if (customPakLocation != null && File(customPakLocation).exists())
            return customPakLocation

        val packageContentPath = "Dungeons/Content/Paks"
        val possibleDrives = "CDEF"

        val launcherContentPath = "XboxGames/Minecraft Dungeons/Content"
        val possibleLauncherLocations = possibleDrives
            .map { "$it:/$launcherContentPath/$packageContentPath" }
            .toTypedArray()

        val steamContentPath = "steamapps/common/MinecraftDungeons"
        val possibleSteamLocations = listOf(
            "${System.getenv("ProgramFiles(x86)")}/Steam",
            *possibleDrives.map { "$it:/SteamLibrary" }.toTypedArray()
        )
            .map { "$it/$steamContentPath/$packageContentPath" }
            .toTypedArray()

        val candidates = listOf(
            "${System.getProperty("user.home")}/AppData/Local/Mojang/products/dungeons/dungeons/Dungeons/Content/Paks",
            *possibleLauncherLocations,
            *possibleSteamLocations
        )
        val candidate = candidates.find { File(it.split("/").joinToString(File.separator)).exists() }
        if (candidate != null) return candidate

        val files = try { Files.walk(Path("${System.getProperty("user.home")}/.local/share/Steam/steamapps/compatdata")) } catch (e: NoSuchFileException) { return null }
        val found = files.toList().find { it.absolutePathString().endsWith("/Dungeons/Content/Paks") }

        return found?.absolutePathString()
    }
}