package dungeons

import blackstone.states.StoredDataState
import blackstone.states.items.*
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt


class DungeonsLevel {
    companion object {
        fun toInGameLevel(serialized: Long): Double = ((1.0 / 30.0) * (sqrt(3 * serialized + 100.0) + 20) * 10000.0).roundToInt() / 10000.0
        fun toSerializedLevel(ingame: Double): Long = (100 * (ingame - 1) * (3 * ingame - 1)).toLong()
    }
}

class DungeonsPower {
    companion object {
        fun toInGamePower(serialized: Double): Double = (max(1.0, serialized) - 1) * 10 + 1
        fun toSerializedPower(ingame: Double) =
            if (ingame <= 0) 0.0
            else (((max(1.0, ingame) - 1) / 10 + 1) * 100000.0).roundToInt() / 100000.0

        fun playerPower(stored: StoredDataState): Int {
            val powerDividedBy4 = listOf(
                stored.items.find(equippedMelee)?.power ?: 0.0,
                stored.items.find(equippedArmor)?.power ?: 0.0,
                stored.items.find(equippedRanged)?.power ?: 0.0
            ).sumOf { toInGamePower(it) } / 4.0
            val powerDividedBy12 = listOf(
                stored.items.find(equippedArtifact1)?.power ?: 0.0,
                stored.items.find(equippedArtifact2)?.power ?: 0.0,
                stored.items.find(equippedArtifact3)?.power ?: 0.0
            ).sumOf { toInGamePower(it) } / 12.0

            return (powerDividedBy4 + powerDividedBy12).toInt()
        }

    }
}
