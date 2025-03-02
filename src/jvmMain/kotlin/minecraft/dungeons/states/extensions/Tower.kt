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

@Stable
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
            result = result.replace(
                it,
                Localizations["tower_tile_label_$it"].trim()
            )
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