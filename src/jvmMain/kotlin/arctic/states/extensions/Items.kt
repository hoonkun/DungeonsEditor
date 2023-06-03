package arctic.states.extensions

import arctic.states.Item

val Item.totalEnchantmentInvestedPoints get() = enchantments?.sumOf { it.investedPoints } ?: 0
fun Item.updateEnchantmentInvestedPoints() { enchantments?.forEach { it.leveling(it.level) } }
val Item.where get() = if (parent.items.contains(this)) "inventory" else if (parent.storageChestItems.contains(this)) "storage" else null
