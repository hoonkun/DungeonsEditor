package minecraft.dungeons.values

import kotlin.math.roundToInt
import kotlin.math.sqrt


object DungeonsLevel {

    fun toInGameLevel(serialized: Long): Double =
        ((1.0 / 30.0) * (sqrt(3 * serialized + 100.0) + 20) * 10000.0).roundToInt() / 10000.0

    fun toSerializedLevel(ingame: Double): Long =
        (100 * (ingame - 1) * (3 * ingame - 1)).toLong()

}
