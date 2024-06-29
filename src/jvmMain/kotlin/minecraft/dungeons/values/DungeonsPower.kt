package minecraft.dungeons.values

import kotlin.math.max
import kotlin.math.roundToInt

object DungeonsPower {

    fun toInGamePower(serialized: Double): Double =
        (max(1.0, serialized) - 1) * 10 + 1

    fun toSerializedPower(ingame: Double) =
        if (ingame <= 0) 0.0
        else (((max(1.0, ingame) - 1) / 10 + 1) * 100000.0).roundToInt() / 100000.0

}
