package blackstone.states.items

import Database
import blackstone.states.Enchantment


val Enchantment.data get() = Database.current.findEnchantment(id) ?: throw RuntimeException("unknown enchantment $id")

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