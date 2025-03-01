package minecraft.dungeons.states.extensions

import minecraft.dungeons.io.DungeonsJsonFile
import minecraft.dungeons.states.MutableDungeons
import java.io.File
import java.time.LocalDateTime

object MutableDungeonsIOExtensionScope {

    fun MutableDungeons.save(
        source: DungeonsJsonFile,
        destination: DungeonsJsonFile,
        createBackup: Boolean = true
    ) {
        val date = LocalDateTime.now().run {
            listOf(year - 2000, monthValue, dayOfMonth, hour, minute, second)
                .joinToString("") { "$it".padStart(2, '0') }
        }

        val json = export()
        if (destination.isDirectory) {
            if (createBackup)
                source.copyTo(File("${destination.absolutePath}/${source.nameWithoutExtension}.b$date.dat"))
            DungeonsJsonFile("${destination.absolutePath}/${source.name}").write(json)
        } else {
            if (createBackup)
                source.copyTo(File("${destination.parentFile.absolutePath}/${source.nameWithoutExtension}.b$date.dat"))
            destination.write(json)
        }
    }

}

fun <T>withDungeonsIO(block: MutableDungeonsIOExtensionScope.() -> T) = MutableDungeonsIOExtensionScope.block()
