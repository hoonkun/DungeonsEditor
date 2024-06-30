package minecraft.dungeons.values

import kotlin.math.max
import kotlin.math.roundToInt

@JvmInline
value class InGameDungeonsPower(val value: Double) {
    override fun toString(): String = value.toString()

    companion object {
        val Zero = InGameDungeonsPower(0.0)
    }
}

fun InGameDungeonsPower.toSerialized() =
    SerializedDungeonsPower(
        if (value <= 0) 0.0
        else (((max(1.0, value) - 1) / 10 + 1) * 100000.0).roundToInt() / 100000.0
    )

fun InGameDungeonsPower.roundToInt() = value.roundToInt()
fun InGameDungeonsPower.truncate() = kotlin.math.truncate(value).roundToInt()

@JvmInline
value class SerializedDungeonsPower(val value: Double) {
    override fun toString(): String = value.toString()
}

fun SerializedDungeonsPower.toInGame() =
    InGameDungeonsPower((max(1.0, value) - 1) * 10 + 1)

fun Double.asInGamePower() = InGameDungeonsPower(this)
fun Double.asSerializedPower() = SerializedDungeonsPower(this)
