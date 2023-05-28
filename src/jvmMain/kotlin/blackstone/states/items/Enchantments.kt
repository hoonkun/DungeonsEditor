package blackstone.states.items

import Database
import blackstone.states.Enchantment
import blackstone.states.Item


val Enchantment.data get() = Database.current.findEnchantment(id) ?: throw RuntimeException("unknown enchantment $id")

fun Enchantment(id: String, holder: Item, investedPoints: Int = 0, level: Int = 0, isNetheriteEnchant: Boolean = false) =
    Enchantment(id, investedPoints, level).apply { this.holder = holder; this.isNetheriteEnchant = isNetheriteEnchant }

fun Enchantment(other: Enchantment) =
    Enchantment(other).apply { this.holder = other.holder; this.isNetheriteEnchant = other.isNetheriteEnchant }

fun Enchantment.changeInto(newId: String) {
    val prevId = id
    id = newId
    leveling(if (newId == "Unset") 0 else level)

    if (isNetheriteEnchant && (prevId == "Unset" || newId == "Unset")) {
        holder.enchantments?.forEach { it.leveling(it.level) }
    }
}

fun Enchantment.leveling(newLevel: Int, isNetheriteEnchant: Boolean = false) {
    level = newLevel

    val nonGlided = holder.netheriteEnchant == null || holder.netheriteEnchant?.id == "Unset"
    investedPoints =
        if (isNetheriteEnchant)
            0
        else if (!data.powerful && nonGlided)
            EnchantmentData.CommonNonGlidedInvestedPoints.slice(0 until newLevel).sum()
        else if (data.powerful && nonGlided)
            EnchantmentData.PowerfulNonGlidedInvestedPoints.slice(0 until newLevel).sum()
        else if (!data.powerful && !nonGlided)
            EnchantmentData.CommonGlidedInvestedPoints.slice(0 until newLevel).sum()
        else if (data.powerful && !nonGlided)
            EnchantmentData.PowerfulGlidedInvestedPoints.slice(0 until newLevel).sum()
        else 0
}