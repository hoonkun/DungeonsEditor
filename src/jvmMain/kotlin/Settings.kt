import java.io.File

class Settings {

    companion object {

        val globalScale: Float

        init {
            val file = File("./settings.arctic")
            val settings =
                if (file.exists())
                    file
                        .readText()
                        .trim()
                        .split("\n")
                        .associate { it.split("=").let { segments -> segments[0] to segments[1] } }
                else
                    emptyMap()

            globalScale = settings["scale"]?.toFloat()?.coerceIn(0.4f..1f) ?: 0.5f
        }

    }

}
