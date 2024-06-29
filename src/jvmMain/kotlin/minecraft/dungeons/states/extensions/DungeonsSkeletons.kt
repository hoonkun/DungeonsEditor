package minecraft.dungeons.states.extensions

import minecraft.dungeons.resources.DungeonsSkeletons
import minecraft.dungeons.states.MutableDungeons


val MutableDungeons.Item.data get() =
    DungeonsSkeletons.Item[type]
        ?: throw DungeonsSkeletons.NotFoundException(type, DungeonsSkeletons.Item)

val MutableDungeons.Enchantment.data get() =
    DungeonsSkeletons.Enchantment[id]
        ?: throw DungeonsSkeletons.NotFoundException(id, DungeonsSkeletons.Enchantment)

val MutableDungeons.ArmorProperty.data get() =
    DungeonsSkeletons.ArmorProperty[id]
        ?: throw DungeonsSkeletons.NotFoundException(id, DungeonsSkeletons.ArmorProperty)
