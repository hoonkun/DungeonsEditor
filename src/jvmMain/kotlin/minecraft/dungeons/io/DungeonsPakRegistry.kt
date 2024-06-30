package minecraft.dungeons.io

import Keyset
import minecraft.dungeons.resources.DungeonsSkeletons
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
    lateinit var index: PakIndex

    fun initialize(customLocation: String? = null): Boolean {
        if (this::index.isInitialized) return true

        val pakPath = detectPakPath(customLocation) ?: return false

        val newIndex = PakIndex(
            target = File(pakPath).listFiles()!!.sortedBy { it.name },
            useCache = true,
            caseSensitive = true,
            filter = PathPakFilter(listOf("/Dungeons/Content"), false)
        )
        newIndex.useKey(Guid.Zero, Keyset.PakKey)

        index = newIndex

        DungeonsSkeletons.Initializer.run(newIndex)
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

        val files =
            try { Files.walk(Path("${System.getProperty("user.home")}/.local/share/Steam/steamapps/compatdata")) }
            catch (e: NoSuchFileException) { return null }

        val found = files.toList().find { it.absolutePathString().endsWith("/Dungeons/Content/Paks") }

        return found?.absolutePathString()
    }
}