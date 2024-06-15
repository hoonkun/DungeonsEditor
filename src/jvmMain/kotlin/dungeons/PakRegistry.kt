package dungeons

import LocalData
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import pak.PakIndex
import pak.PathPakFilter
import parsers.objects.fields.Guid
import java.io.File
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.jvm.optionals.getOrNull



class PakRegistry {

    companion object {

        private var _index: PakIndex? by mutableStateOf(null)
        val index by derivedStateOf { _index!! }

        fun initialize(): Boolean {
            if (_index != null) return true

            val pakPath = findPakPath() ?: return false

            val newIndex = PakIndex(
                File(pakPath).listFiles()!!.sortedBy { it.name },
                useCache = true,
                caseSensitive = true,
                filter = PathPakFilter(listOf("/Dungeons/Content"), false)
            )
            newIndex.useKey(Guid.Zero, Keyset.PakKey)

            _index = newIndex

            Database.register(DatabaseGenerator.parsePak(newIndex))

            return true
        }

        fun findPakPath(): String? {
            val customPakLocation = LocalData.customPakLocation
            if (customPakLocation != null && File(customPakLocation).exists()) return customPakLocation

            val candidates = listOf(
                "${System.getProperty("user.home")}/AppData/Local/Mojang/products/dungeons/dungeons/Dungeons/Content/Paks",
                "${System.getenv("ProgramFiles(x86)")}/Steam/steamapps/common/MinecraftDungeons/Dungeons/Content/Paks",
                "C:/XboxGames/Minecraft Dungeons/Content/Dungeons/Content/Paks"
            )
            val candidate = candidates.find { File(it.split("/").joinToString(File.separator)).exists() }
            if (candidate != null) return candidate

            val files = try { Files.walk(Path("${System.getProperty("user.home")}/.local/share/Steam/steamapps/compatdata")) } catch (e: NoSuchFileException) { return null }
            val found = files.filter { it.absolutePathString().endsWith("/Dungeons/Content/Paks") }.findFirst()

            return found.getOrNull()?.absolutePathString()
        }

    }

}