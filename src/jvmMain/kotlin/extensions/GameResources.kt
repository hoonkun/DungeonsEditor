package extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class GameResources {
    companion object {
        fun image(key: String? = null, useSuffix: Boolean = true, preprocess: (BufferedImage) -> BufferedImage = { it }, pathFactory: () -> String): ImageBitmap =
            SharedImages.getOrPut(key ?: pathFactory()) {
                preprocess(ImageIO.read(File("${if (useSuffix) Constants.GameDataDirectoryPath else ""}${pathFactory()}"))).toComposeImageBitmap()
            }

        fun image(key: String): ImageBitmap? = SharedImages[key]

        private val SharedImages: MutableMap<String, ImageBitmap> = mutableMapOf()
    }
}

class Resources {
    companion object {
        fun image(key: String? = null, pathFactory: () -> String): ImageBitmap =
            SharedImages.getOrPut(key ?: pathFactory()) {
                ImageIO.read({}::class.java.getResource(pathFactory())!!.toURI().toURL()).toComposeImageBitmap()
            }

        private val SharedImages: MutableMap<String, ImageBitmap> = mutableMapOf()
    }
}
