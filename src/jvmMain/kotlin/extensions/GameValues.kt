package extensions

import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt


class DungeonsLevel {
    companion object {
        fun toInGameLevel(serialized: Long): Float = ((1f / 30f) * (sqrt(3 * serialized + 100f) + 20) * 10000f).roundToInt() / 10000f
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
