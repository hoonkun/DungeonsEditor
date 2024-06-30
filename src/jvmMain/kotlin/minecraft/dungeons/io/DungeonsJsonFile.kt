package minecraft.dungeons.io

import Keyset
import androidx.compose.runtime.Stable
import androidx.compose.runtime.toMutableStateList
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.emerald
import minecraft.dungeons.states.extensions.gold
import minecraft.dungeons.values.*
import org.json.JSONObject
import utils.transformWithJsonObject
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory


class DungeonsJsonFile(path: String): File(path) {

    constructor(file: File): this(file.absolutePath)

    private fun magicValid(): Boolean {
        val array = ByteArray(4)
        val buffer = ByteBuffer.wrap(array)

        val channel = inputStream().channel

        buffer.mark()
        channel.read(buffer)
        if (!array.contentEquals(Magic1))
            return false

        buffer.reset()
        channel.read(buffer)
        return array.contentEquals(Magic2)
    }

    fun validate(): Boolean {
        if (!exists() || isDirectory || !isFile) return false

        return magicValid()
    }

    fun preview(): Preview {
        return if (!exists() || isDirectory || !isFile)
            Preview.None
        else if (!magicValid())
            Preview.Invalid
        else
            Preview.Valid(absolutePath, read())
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

    object Detector {
        private val ParentPaths = listOf("Saved Games", "저장된 게임")
        private val DirectoryIdentifier = ParentPaths.map {
            "/$it/Mojang Studios/Dungeons".replace("/", separator)
        }

        val results = detectDungeonsJson()
            .let { it.slice(0 until 3.coerceAtMost(it.size)) }
            .mapNotNull {
                try { DungeonsJsonFile(it).preview() }
                catch (e: Exception) { null }
            }
            .filterIsInstance<Preview.Valid>()

        private fun detectDungeonsJson(): List<String> {
            val windowsFiles = detectWindows()
            val linuxFiles = detectLinux()

            val result = mutableListOf<String>()
            result.addAll(windowsFiles)
            result.addAll(linuxFiles)

            return result
        }

        private fun detectWindows(base: String = System.getProperty("user.home")): List<String> =
            DirectoryIdentifier.flatMap { directory ->
                File("$base$directory").listFiles()
                    ?.flatMap { File("${it.absolutePath}${separator}Characters").listFiles()?.toList() ?: emptyList() }
                    ?.map { it.absolutePath }
                    ?: emptyList()
            }

        private fun detectLinux(): List<String> {
            val baseLocation = Path("${System.getProperty("user.home")}/.local/share/Steam/steamapps/compatdata/")
            if (!baseLocation.exists()) return emptyList()
            return DirectoryIdentifier.flatMap { directory ->
                Files.walk(baseLocation)
                    .toList()
                    .filter { it.absolutePathString().endsWith(directory) && it.isDirectory() }
                    .flatMap { detectWindows(it.absolutePathString().removeSuffix(directory)) }
            }
        }
    }

    @Stable
    sealed interface Preview {
        data object None: Preview
        data object Invalid: Preview

        @Stable
        class Valid(
            val path: String,
            from: JSONObject
        ): Preview {
            private val currencies = from.getJSONArray(MutableDungeons.FIELD_CURRENCIES)
                .transformWithJsonObject { MutableDungeons.Currency(it) }
                .toMutableStateList()

            private val items = from.getJSONArray(MutableDungeons.FIELD_ITEMS)
                .transformWithJsonObject(6) { MutableDungeons.Item(it) }

            val level: Int = from.getLong(MutableDungeons.FIELD_XP)
                .asSerializedLevel()
                .toInGame()
                .truncate()

            val emerald = currencies.emerald()?.count ?: 0
            val gold = currencies.gold()?.count ?: 0

            val melee = items.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.Melee }
            val armor = items.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.Armor }
            val ranged = items.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.Ranged }

            private val artifact1 = items.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.HotBar1 }
            private val artifact2 = items.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.HotBar2 }
            private val artifact3 = items.find { it.equipmentSlot == DungeonsItem.EquipmentSlot.HotBar3 }

            val power = run {
                val powerDividedBy4 = listOfNotNull(melee, armor, ranged)
                    .sumOf { it.power.value }
                    .div(4.0)

                val powerDividedBy12 = listOfNotNull(artifact1, artifact2, artifact3)
                    .sumOf { it.power.value }
                    .div(12.0)

                (powerDividedBy4 + powerDividedBy12).asInGamePower()
            }
        }
    }

    companion object {
        private val Magic1 = byteArrayOf(0x44, 0x30, 0x30, 0x31)
        private val Magic2 = byteArrayOf(0x00, 0x00, 0x00, 0x00)
    }

}
