package minecraft.dungeons.resources

import org.json.JSONObject
import pak.PakIndex
import utils.transformWithJsonObject
import java.nio.charset.StandardCharsets

object DungeonsTower {

    private lateinit var _mobGroupConfig: JSONObject
    val mobGroupConfig get() = _mobGroupConfig

    private val _tiles = mutableListOf<String>()
    val tiles: List<String> get() = _tiles

    private val _challenges = mutableListOf<String>()
    val challenges: List<String> get() = _challenges

    object Initializer {
        fun run(index: PakIndex) {
            val path = index.filter { it.endsWith("thetower.json") }.first()
            val rawString = index.getFileBytes(path)!!
                .toString(StandardCharsets.UTF_8)
                .trim()
                .split("\n")
                .drop(6)
                .joinToString("\n")

            val json = JSONObject(rawString)

            _mobGroupConfig = JSONObject().apply { put("mob-groups", json.getJSONArray("mob-groups")) }

            _tiles.clear()
            _tiles.addAll(json.getJSONArray("tiles").transformWithJsonObject { it.getString("id") })

            _challenges.clear()
            _challenges.addAll(json.getJSONArray("challenges").transformWithJsonObject { it.getString("id") })
        }
    }

}