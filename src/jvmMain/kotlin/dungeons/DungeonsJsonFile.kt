package dungeons

import org.json.JSONObject
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


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
