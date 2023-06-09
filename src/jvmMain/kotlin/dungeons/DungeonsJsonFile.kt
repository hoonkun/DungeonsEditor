package dungeons

import dungeons.states.DungeonsJsonState
import dungeons.states.Item
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory


private val Magic1 = listOf(0x44, 0x30, 0x30, 0x31).map { it.toByte() }.toByteArray()
private val Magic2 = listOf(0x00, 0x00, 0x00, 0x00).map { it.toByte() }.toByteArray()


fun File.readDungeonsJson(): JSONObject {
    val content = readBytes().let { it.sliceArray(8 until it.size) }

    val cipher = Cipher
        .getInstance("AES/ECB/NoPadding")
        .apply { init(Cipher.DECRYPT_MODE, SecretKeySpec(Keyset.StoreKey, "AES")) }

    val output = cipher.doFinal(content)

    return JSONObject(String(output).trim())
}

fun File.writeDungeonsJson(json: JSONObject) {
    val input = json.toString(1).let { it.padEnd(it.length + 16 - (it.length % 16), ' ') }

    val cipher = Cipher
        .getInstance("AES/ECB/NoPadding")
        .apply { init(Cipher.ENCRYPT_MODE, SecretKeySpec(Keyset.StoreKey, "AES")) }

    val content = cipher.doFinal(input.toByteArray())

    writeBytes(byteArrayOf(*Magic1, *Magic2, *content))
}

fun File.readDungeonsSummary(): Triple<String, DungeonsJsonState, DungeonsSummary> {
    val state = DungeonsJsonState(readDungeonsJson(), this)
    return Triple(
        path,
        state,
        DungeonsSummary(
            state.playerLevel.toInt(),
            state.playerPower,
            state.currencies.find { it.type == "Emerald" }?.count ?: 0,
            state.currencies.find { it.type == "Gold" }?.count ?: 0,
            state.equippedMelee,
            state.equippedArmor,
            state.equippedRanged
        )
    )
}

class DungeonsJsonFile {
    companion object {
        val detected = detectDungeonsJson()
            .let { it.slice(0 until 3.coerceAtMost(it.size)) }
            .mapNotNull {
                try { File(it).readDungeonsSummary() }
                catch (e: Exception) { null }
            }
    }
}

class DungeonsSummary(
    val level: Int,
    val power: Int,
    val emerald: Int,
    val gold: Int,
    val melee: Item?,
    val armor: Item?,
    val ranged: Item?
)


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
