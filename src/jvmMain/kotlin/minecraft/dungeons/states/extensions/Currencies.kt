package minecraft.dungeons.states.extensions

import minecraft.dungeons.states.MutableDungeons


fun List<MutableDungeons.Currency>.gold() = find { it.type == "Gold" }
fun List<MutableDungeons.Currency>.emerald() = find { it.type == "Emerald" }

object MutableDungeonsCurrencyExtensionScope {

    private val MutableDungeons._gold get() = currencies.gold()
    private val MutableDungeons._emerald get() = currencies.emerald()

    var MutableDungeons.gold: Int
        get() = _gold?.count ?: 0
        set(value) {
            val target = _gold ?: MutableDungeons.Currency(type = "Gold", count = 0).also { currencies.add(it) }
            target.count = value
        }

    var MutableDungeons.emerald: Int
        get() = _emerald?.count ?: 0
        set(value) {
            val target = _emerald ?: MutableDungeons.Currency(type = "Emerald", count = 0).also { currencies.add(it) }
            target.count = value
        }

}

fun <T>withCurrencies(block: MutableDungeonsCurrencyExtensionScope.() -> T) = MutableDungeonsCurrencyExtensionScope.block()