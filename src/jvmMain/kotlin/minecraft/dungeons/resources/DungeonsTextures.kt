package minecraft.dungeons.resources

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kiwi.hoonkun.utils.nameWithoutExtension
import kiwi.hoonkun.utils.removeExtension
import minecraft.dungeons.io.DungeonsPakRegistry
import pak.PakIndex
import parsers.Texture2d
import utils.TextureDecoder
import java.awt.Point
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.random.Random
import kotlin.random.nextInt


object DungeonsTextures {

    private val lock = ReentrantLock()

    private val cache: MutableMap<String, ImageBitmap> = mutableMapOf()

    operator fun get(
        path: String,
        cacheKey: String = path,
    ): ImageBitmap =
        cache.getOrPut(cacheKey) {
            val rawPath = path.removeExtension()
            val pakPath =
                if (!rawPath.startsWith("/Dungeons/Content")) "/Dungeons/Content$rawPath"
                else rawPath

            val (width, height, bytes) = lock.withLock { DungeonsPakRegistry.index.getPackage(pakPath) }
                ?.getExport<Texture2d>()
                ?.let { try { TextureDecoder.decode(it) } catch (e: Exception) { null } }
                ?: throw RuntimeException("could not load texture: $pakPath")

            BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
                .apply { data = Raster.createRaster(sampleModel, DataBufferByte(bytes, bytes.size), Point()) }
                .toComposeImageBitmap()
        }

    fun cached(key: String): ImageBitmap? = cache[key]

    object Pets {
        operator fun invoke(unused: Unit): List<ImageBitmap> = unused.let { DataHolder.pets }
        operator fun get(randomKey: String): ImageBitmap =
            DataHolder.pets[Random(randomKey.hashCode()).nextInt(0 until DataHolder.pets.size)]
    }

    object Initializer {
        fun run(index: PakIndex) {
            val excluded = listOf(
                "cape_cosmetic", "emote_cosmetic", "_levelup_cosmetic", "_healing_cosmetic", "_mob_cosmetic", "_respawn_cosmetic",
                "filter_cosmetic", "levelupflair_cosmetic", "healingflair_cosmetic", "mobflair_cosmetic", "respawnflair_cosmetic"
            )

            DataHolder.pets.clear()

            index
                .filter { path ->
                    val lowercased = path.lowercase()
                    val name = lowercased.nameWithoutExtension()

                    lowercased.endsWith("_cosmetic.uasset") && excluded.all { !name.endsWith(it) }
                }
                .forEach {
                    val pakPackage = DungeonsPakRegistry.index.getPackage(it.removeExtension())
                        ?: return@forEach
                    val texture = pakPackage.getExport<Texture2d>()
                        ?: return@forEach
                    val (width, height, bytes) =
                        try { TextureDecoder.decode(texture) }
                        catch (e: Exception) { return@forEach }

                    DataHolder.pets.add(
                        BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
                            .apply { data = Raster.createRaster(sampleModel, DataBufferByte(bytes, bytes.size), Point()) }
                            .toComposeImageBitmap()
                    )
                }
        }
    }

    private object DataHolder {
        val pets: MutableList<ImageBitmap> = mutableListOf()
    }

}
