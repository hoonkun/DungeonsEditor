package arctic.states.extensions

import arctic.states.Enchantment
import dungeons.EnchantmentData


fun Enchantment.leveling(newLevel: Int) {
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
