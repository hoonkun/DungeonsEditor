package minecraft.dungeons.resources

import kiwi.hoonkun.resources.Localizations
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

    private val areaShortNames = listOf(
        "pp", "dt", "hh", "crc",
        "ot", "ls", "gs", "bd",
        "ts", "ew", "frf", "frf1",
        "lf", "ss", "ff", "am",
        "cc", "wp", "bc"
    )
    private val areaNames = listOf(
        "pumpkinpastures", "deserttemple", "highblockhalls", "creeperwoodsbonus",
        "overgrowntemple", "lostsettlement", "galesanctum", "basaltdeltas",
        "thestronghold", "enderwilds", "frozenfjord", "frozenfjord",
        "lonelyfortress", "soggyswamp", "fieryforge", "abyssalmonument",
        "cacticanyon", "windsweptpeaks", "blightedcitadel"
    )
    val AreaDungeonsLocalizations = areaShortNames.zip(areaNames)
    val AreaLocalizations = listOf(
        "boss", "arena", "igloo", "top_tempest", "tempest", "inhabitant",
        "puzzle", "alt", "entrance", "bottom", "top", "sec"
    )

    val ChallengeCommonLocalizations = Localizations.keys()
        .filter { it.startsWith("tower_challenge_common_") }
        .map { it.removePrefix("tower_challenge_common_") }
        .sortedByDescending { it.length }
    val ChallengeExactLocalizations = Localizations.keys()
        .filter { it.startsWith("tower_challenge_exact_") }
        .map { it.removePrefix("tower_challenge_exact_") }
        .sortedByDescending { it.length }

    val AreaMediumNames = listOf(
        "soggyswamp" to "ss", "abyssal" to "am", "cacti" to "cc", "windswept" to "wp"
    )

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