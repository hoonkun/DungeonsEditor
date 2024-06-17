package minecraft.dungeons.resources

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import minecraft.dungeons.io.DungeonsPakRegistry
import parsers.Texture2d
import utils.TextureDecoder
import java.awt.Point
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


object DungeonsTextures {
    private val lock = ReentrantLock()

    operator fun get(key: String): ImageBitmap = get { key }

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

            val pakPackage = lock.withLock { DungeonsPakRegistry.index.getPackage(pakPath) }
                ?: throw RuntimeException("could not find ingame resource in pak file: $pakPath")

            val texture = pakPackage.getExport<Texture2d>()
                ?: throw RuntimeException("could not find texture data in pak package: $pakPath")

            val decoded = TextureDecoder.decode(texture)

            val image = BufferedImage(decoded.width, decoded.height, BufferedImage.TYPE_4BYTE_ABGR)
            image.data = Raster.createRaster(image.sampleModel, DataBufferByte(decoded.data, decoded.data.size), Point())

            preprocess(image).toComposeImageBitmap()
        }

    fun cached(key: String): ImageBitmap? = cache[key]

    private val cache: MutableMap<String, ImageBitmap> = mutableMapOf()
}
