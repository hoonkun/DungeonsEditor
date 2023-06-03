package dungeons.states.extensions

import dungeons.states.ArmorProperty
import dungeons.states.Enchantment
import dungeons.states.Item
import dungeons.Database


val Enchantment.data get() = Database.enchantment(id) ?: throw RuntimeException("unknown enchantment $id")

val ArmorProperty.data get() = Database.armorProperty(id) ?: throw RuntimeException("unknown armor property $id")

val Item.data get() = Database.item(type) ?: throw RuntimeException("Unknown item $type")