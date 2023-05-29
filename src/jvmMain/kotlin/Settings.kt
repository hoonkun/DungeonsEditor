import java.io.File

class Settings {

    companion object {

        var globalScale: Float = 0.5f

        init {
            val settingsFile = File("./settings.arctic")
            if (settingsFile.exists()) {
                val map = settingsFile.readText().trim().split("\n").associate { it.split("=").let { seg -> seg[0] to seg[1] } }
                globalScale = map["scale"]?.toFloat()?.coerceIn(0.4f..1f) ?: 0.5f
            }
        }

    }

}
