package blackstone.states.common

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import blackstone.states.StoredDataState
import blackstone.states.items.*
import extensions.DungeonsPower
import stored

class Common(parent: StoredDataState) {

    val power by derivedStateOf {
        val powerDividedBy4 = listOf(
            parent.items.find(equippedMelee)?.power ?: 0.0,
            parent.items.find(equippedArmor)?.power ?: 0.0,
            parent.items.find(equippedRanged)?.power ?: 0.0
        ).sumOf { DungeonsPower.toInGamePower(it) } / 4.0
        val powerDividedBy12 = listOf(
            parent.items.find(equippedArtifact1)?.power ?: 0.0,
            parent.items.find(equippedArtifact2)?.power ?: 0.0,
            parent.items.find(equippedArtifact3)?.power ?: 0.0
        ).sumOf { DungeonsPower.toInGamePower(it) } / 12.0

        (powerDividedBy4 + powerDividedBy12).toInt()
    }

    val emeralds by derivedStateOf {
        parent.currencies.find { it.type == "Emerald" }?.count ?: 0
    }

    val golds by derivedStateOf {
        parent.currencies.find { it.type == "Gold" }?.count ?: 0
    }

    val eyeOfEnder by derivedStateOf {
        parent.currencies.find { it.type == "EyeOfEnder" }?.count ?: 0
    }

}

val commonData = Common(stored)

val StoredDataState.common get() = commonData
