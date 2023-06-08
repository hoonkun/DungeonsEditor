package dungeons

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import extensions.decode
import parsers.Texture2d
import java.awt.Point
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import java.io.File
import javax.imageio.ImageIO

class IngameImages {
    companion object {
        fun get(
            key: String? = null,
            preprocess: (BufferedImage) -> BufferedImage = { it },
            pathFactory: () -> String
        ): ImageBitmap =
            cache.getOrPut(key ?: pathFactory()) {
                val rawPath = pathFactory().replaceAfterLast('.', "").removeSuffix(".")
                val pakPath =
                    if (!rawPath.startsWith("/Dungeons/Content")) "/Dungeons/Content".plus(rawPath.removePrefix("/Game"))
                    else rawPath

                val pakPackage = PakRegistry.index.getPackage(pakPath)
                    ?: throw RuntimeException("could not find ingame resource in pak file: $pakPath")

                val texture = pakPackage.getExport<Texture2d>()
                    ?: throw RuntimeException("could not find texture data in pak package: $pakPath")

                val decoded = texture.decode()

                val image = BufferedImage(decoded.width, decoded.height, BufferedImage.TYPE_4BYTE_ABGR)
                image.data = Raster.createRaster(image.sampleModel, DataBufferByte(decoded.data, decoded.data.size), Point())

                preprocess(image).toComposeImageBitmap()
            }

        fun cached(key: String): ImageBitmap? = cache[key]

        private val cache: MutableMap<String, ImageBitmap> = mutableMapOf()
    }
}
