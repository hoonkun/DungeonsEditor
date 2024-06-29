package minecraft.dungeons.states.extensions

import minecraft.dungeons.resources.DungeonsSkeletons
import minecraft.dungeons.states.MutableDungeons


val MutableDungeons.Item.skeleton get() =
    DungeonsSkeletons.Item[type]
        ?: throw DungeonsSkeletons.NotFoundException(type, DungeonsSkeletons.Item)

val MutableDungeons.Enchantment.skeleton get() =
    DungeonsSkeletons.Enchantment[id]
        ?: throw DungeonsSkeletons.NotFoundException(id, DungeonsSkeletons.Enchantment)

val MutableDungeons.ArmorProperty.skeleton get() =
    DungeonsSkeletons.ArmorProperty[id]
        ?: throw DungeonsSkeletons.NotFoundException(id, DungeonsSkeletons.ArmorProperty)
