package dungeons

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class IngameImages {
    companion object {
        fun get(
            key: String? = null,
            useSuffix: Boolean = true,
            preprocess: (BufferedImage) -> BufferedImage = { it },
            pathFactory: () -> String
        ): ImageBitmap =
            cache.getOrPut(key ?: pathFactory()) {
                val path = "${if (useSuffix) Constants.GameDataDirectoryPath else ""}${pathFactory()}"
                preprocess(ImageIO.read(File(path))).toComposeImageBitmap()
            }

        fun cached(key: String): ImageBitmap? = cache[key]

        private val cache: MutableMap<String, ImageBitmap> = mutableMapOf()
    }
}
