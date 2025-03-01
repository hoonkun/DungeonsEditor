package minecraft.dungeons.values

import utils.toFixed
import kotlin.math.roundToInt
import kotlin.math.sqrt


@JvmInline
value class InGameDungeonsLevel(val value: Double) {
    override fun toString(): String = value.toString()
}

fun InGameDungeonsLevel.toSerialized() =
    SerializedDungeonsLevel((100 * (value - 1) * (3 * value - 1)).toLong())

fun InGameDungeonsLevel.truncate() = kotlin.math.truncate(value).roundToInt()
fun InGameDungeonsLevel.toFixed(digits: Int) = value.toFixed(digits)

@JvmInline
value class SerializedDungeonsLevel(val value: Long) {
    override fun toString(): String = value.toString()
}

fun SerializedDungeonsLevel.toInGame() =
    InGameDungeonsLevel(((1.0 / 30.0) * (sqrt(3 * value + 100.0) + 20)).toFixed(4))

fun Double.asInGameLevel() = InGameDungeonsLevel(this)
fun Long.asSerializedLevel() = SerializedDungeonsLevel(this)
