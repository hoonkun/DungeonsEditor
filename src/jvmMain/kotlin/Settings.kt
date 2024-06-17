import androidx.compose.runtime.Immutable
import java.io.File

@Immutable
data object Settings {

    val globalScale: Float

    val preloadTextures: Boolean

    init {
        val pwd = System.getenv("APPIMAGE")?.let { it.dropLast(it.length - it.lastIndexOf('/')) } ?: "."
        val file = File("$pwd/settings.arctic")
        val settings =
            if (file.exists())
                file
                    .readText()
                    .trim()
                    .split("\n")
                    .associate { it.split("=").let { segments -> segments[0] to segments[1] } }
            else
                emptyMap()

        globalScale = settings["scale"]?.toFloat()?.coerceIn(0.4f..1.35f) ?: 0.5f
        preloadTextures = settings["preload_textures"].let { it == "true" || it == null }
    }

}
