package minecraft.dungeons.states.extensions

import minecraft.dungeons.states.MutableDungeons


object MutableDungeonsEnchantmentExtensionScope {

    private val CommonNonGlidedInvestedPoints = listOf(1, 2, 3)
    private val PowerfulNonGlidedInvestedPoints = listOf(2, 3, 4)
    private val CommonGlidedInvestedPoints = listOf(2, 3, 4)
    private val PowerfulGlidedInvestedPoints = listOf(3, 4, 5)

    fun MutableDungeons.Enchantment.applyInvestedPoints(
        glided: Boolean,
        newLevel: Int = this.level
    ) {
        level = newLevel

        investedPoints =
            if (newLevel > MutableDungeons.Enchantment.IntendedMaxLevel)
                0
            else if (isNetheriteEnchant)
                0
            else if (!skeleton.powerful && !glided)
                CommonNonGlidedInvestedPoints.slice(0 until newLevel).sum()
            else if (skeleton.powerful && !glided)
                PowerfulNonGlidedInvestedPoints.slice(0 until newLevel).sum()
            else if (!skeleton.powerful && glided)
                CommonGlidedInvestedPoints.slice(0 until newLevel).sum()
            else if (skeleton.powerful && glided)
                PowerfulGlidedInvestedPoints.slice(0 until newLevel).sum()
            else 0
    }

}

fun <T>withEnchantments(block: MutableDungeonsEnchantmentExtensionScope.() -> T) = MutableDungeonsEnchantmentExtensionScope.block()