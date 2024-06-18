package minecraft.dungeons.io

import Keyset
import androidx.compose.runtime.Stable
import kiwi.hoonkun.ui.states.DungeonsJsonState
import kiwi.hoonkun.ui.states.Item
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory


class DungeonsJsonFile(path: String): File(path) {

    constructor(file: File): this(file.absolutePath)

    companion object {
        private val Magic1 = listOf(0x44, 0x30, 0x30, 0x31).map { it.toByte() }.toByteArray()
        private val Magic2 = listOf(0x00, 0x00, 0x00, 0x00).map { it.toByte() }.toByteArray()
    }

    object Detector {
        val results = detectDungeonsJson()
            .let { it.slice(0 until 3.coerceAtMost(it.size)) }
            .mapNotNull {
                try { DungeonsJsonFile(it).summary() }
                catch (e: Exception) { null }
            }

        private fun detectDungeonsJson(): List<String> {
            val windowsFiles = detectWindows()
            val linuxFiles = detectLinux()

            val result = mutableListOf<String>()
            result.addAll(windowsFiles)
            result.addAll(linuxFiles)

            return result
        }

        private val DirectoryIdentifier = "/Saved Games/Mojang Studios/Dungeons".replace("/", File.separator)

        private fun detectWindows(base: String = System.getProperty("user.home")): List<String> {
            val location = "$base$DirectoryIdentifier"
            val characters = File(location).listFiles()?.flatMap { File("${it.absolutePath}${File.separator}Characters").listFiles()?.toList() ?: emptyList() } ?: emptyList()
            return characters.map { it.absolutePath }
        }

        private fun detectLinux(): List<String> {
            val baseLocation = Path("${System.getProperty("user.home")}/.local/share/Steam/steamapps/compatdata/")
            if (!baseLocation.exists()) return emptyList()
            val bases = Files.walk(baseLocation)
                .filter { it.absolutePathString().endsWith(DirectoryIdentifier) && it.isDirectory() }
            val characters = mutableListOf<String>()
            bases.forEach { characters.addAll(detectWindows(it.absolutePathString().removeSuffix(DirectoryIdentifier))) }
            return characters
        }
    }

    sealed interface Preview {
        data object None: Preview
        data object Invalid: Preview

        class Valid(val json: DungeonsJsonState, val summary: DungeonsSummary): Preview
    }

    fun preview(): Preview {
        if (!exists()) return Preview.None
        if (isDirectory) return Preview.None
        if (!isFile) return Preview.None
        return if (inputStream().run { readNBytes(4).contentEquals(Magic1) && readNBytes(4).contentEquals(Magic2) })
            DungeonsJsonState(read(), this).let { Preview.Valid(it, DungeonsSummary.fromState(it)) }
        else
            Preview.Invalid
    }

    fun read(): JSONObject {
        val content = readBytes().let { it.sliceArray(8 until it.size) }

        val cipher = Cipher
            .getInstance("AES/ECB/NoPadding")
            .apply { init(Cipher.DECRYPT_MODE, SecretKeySpec(Keyset.StoreKey, "AES")) }

        val output = cipher.doFinal(content)

        return JSONObject(String(output).trim())
    }

    fun write(json: JSONObject) {
        val input = json.toString(1).let { it.padEnd(it.length + 16 - (it.length % 16), ' ') }

        val cipher = Cipher
            .getInstance("AES/ECB/NoPadding")
            .apply { init(Cipher.ENCRYPT_MODE, SecretKeySpec(Keyset.StoreKey, "AES")) }

        val content = cipher.doFinal(input.toByteArray())

        writeBytes(byteArrayOf(*Magic1, *Magic2, *content))
    }

    fun summary(): Triple<String, DungeonsJsonState, DungeonsSummary> =
        DungeonsJsonState(read(), this).let {
            Triple(path, it, DungeonsSummary.fromState(it))
        }

}

@Stable
class DungeonsSummary(
    val level: Int,
    val power: Int,
    val emerald: Int,
    val gold: Int,
    val melee: Item?,
    val armor: Item?,
    val ranged: Item?
) {
    companion object {
        fun fromState(state: DungeonsJsonState) = state.run {
            DungeonsSummary(
                playerLevel.toInt(),
                playerPower,
                currencies.find { it.type == "Emerald" }?.count ?: 0,
                currencies.find { it.type == "Gold" }?.count ?: 0,
                equippedMelee,
                equippedArmor,
                equippedRanged
            )
        }
    }
}