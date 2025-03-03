package minecraft.dungeons.states.extensions

import androidx.compose.runtime.Stable
import kiwi.hoonkun.resources.Localizations
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.resources.DungeonsTower
import minecraft.dungeons.states.MutableDungeons


@Stable
fun MutableDungeons.TowerMissionState.Info.Config.Floor.Reward.previewBitmap() =
    DungeonsTextures["/Actors/Items/MysteryBoxes/MysteryBox${this}/T_MysteryBox${this}_Icon.png"]

fun LocalizeTowerTile(tile: String): String {
    if (tile.startsWith("twr_floor_")) {
        var result = tile.removePrefix("twr_floor_")
        DungeonsTower.AreaLocalizations.forEach {
            result = result.replace(it, Localizations["tower_tile_label_$it"].trim())
        }
        return result.removePrefix("_g").replace("_", " ")
    } else {
        var result = tile.removePrefix("twr_")
        DungeonsTower.AreaLocalizations.forEach {
            result = result.replace(it, Localizations["tower_tile_label_$it"].trim())
        }
        DungeonsTower.AreaDungeonsLocalizations.forEach { (shortName, localizationName) ->
            result = result.replace(
                shortName,
                DungeonsLocalizations["Mission/${localizationName}_name"]!!.trim().plus(" ")
            )
        }
        return result
            .replace("  ", " ")
            .removePrefix("_g")
            .replace("_floor_", "")
            .replace("_", " ")
    }
}

fun LocalizeTowerChallenge(challenge: String): String {
    var result = challenge

    if (result.startsWith("twr_"))
        result = result.removePrefix("twr_")

    if (result.startsWith("twr-"))
        result = result.removePrefix("twr-")

    if (result.startsWith("boss-twr-"))
        result= "tower_boss_".plus(result.removePrefix("boss-twr-"))

    if (result.contains("_floor_"))
        result = result.replace("_floor_", "_")

    DungeonsTower.ChallengeExactLocalizations.forEach {
        if (result == it) return Localizations["tower_challenge_exact_$it"]
    }

    DungeonsTower.AreaMediumNames.forEach { (old, replaceWith) ->
        if (result.startsWith("${old}_")) result = "${replaceWith}_".plus(result.removePrefix("${old}_"))
        else if (result.contains("_${old}_")) result = result.replace("_${old}_", "_${replaceWith}_")
    }

    DungeonsTower.AreaDungeonsLocalizations.forEach { (shortName, localizationName) ->
        val replaceWith = DungeonsLocalizations["Mission/${localizationName}_name"]!!.trim()
        result =
            if (result.startsWith("${shortName}_"))
                replaceWith.plus(result.removePrefix(shortName))
            else
                result.replace("_${shortName}_", "_".plus(replaceWith).plus("_"))
    }

    DungeonsTower.ChallengeCommonLocalizations.forEach {
        result = result.replace(it, Localizations["tower_challenge_common_$it"])
    }

    return result.replace("_", "/")
}