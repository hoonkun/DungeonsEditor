package dungeons

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
    }
}
