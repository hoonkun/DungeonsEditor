package extensions

import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt


class DungeonsLevel {
    companion object {
        fun toInGameLevel(serialized: Long): Float = ((1f / 30f) * (sqrt(3 * serialized + 100f) + 20) * 10000f).roundToInt() / 10000f
        fun toSerializedLevel(ingame: Float): Long = (100 * (ingame - 1) * (3 * ingame - 1)).toLong()
    }
}

class DungeonsPower {
    companion object {
        fun toInGamePower(serialized: Float): Float = (max(1f, serialized) - 1 + 0.00001f) * 10 + 1
        fun toSerializedPower(ingame: Float) =
            if (ingame <= 0) 0f
            else (((max(1f, ingame) - 1) / 10 - 0.00001f + 1) * 100000f).roundToInt() / 100000f
    }
}
