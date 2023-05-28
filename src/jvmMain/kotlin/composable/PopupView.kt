package composable

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import arctic
import blackstone.states.ArmorProperty
import blackstone.states.Enchantment
import blackstone.states.Item
import composable.blackstone.popup.ArmorPropertyCollection
import composable.blackstone.popup.ArmorPropertyDetail
import composable.blackstone.popup.EnchantmentDetail
import composable.blackstone.popup.EnchantmentsCollection

@Composable
fun BoxScope.Popups() {
    Debugging.recomposition("Popups")

    EnchantmentPopup()
    ArmorPropertyPopup()
}

@Composable
fun EnchantmentPopup() {
    val _target by remember { derivedStateOf { arctic.enchantments.detailTarget } }
    val _shadow by remember { derivedStateOf { arctic.enchantments.shadowDetailTarget } }

    val collectionTargetState = EnchantCollectionPopupTargetStates(
        holder = _target?.holder,
        index = _target?.holder?.enchantments?.indexOf(_target),
        isNetheriteEnchant = _target?.isNetheriteEnchant == true
    )
    val detailTargetState = EnchantDetailPopupTargetStates(
        target = _target,
        isUnset = _target?.id == "Unset"
    )

    Backdrop(arctic.enchantments.hasDetailTarget) { arctic.enchantments.closeDetail() }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedCollection(collectionTargetState) { (holder, index, isNetheriteEnchant) ->
            if (holder != null && index != null) EnchantmentsCollection(holder, index, isNetheriteEnchant)
            else Box(modifier = Modifier.width(700.dp).fillMaxHeight())
        }
        AnimatedDetail(detailTargetState) { (target, isUnset) ->
            if (target != null && !isUnset) EnchantmentDetail(target)
            else Box(modifier = Modifier.width(0.dp).height(500.dp))

            if (target == null && _shadow != null && _shadow?.id != "Unset")
                Box(modifier = Modifier.width(675.dp).height(500.dp))
            else if (target == null && _shadow != null && _shadow?.id == "Unset")
                Box(modifier = Modifier.width(0.dp).height(500.dp))
        }
    }
}

data class EnchantCollectionPopupTargetStates(
    val holder: Item?,
    val index: Int?,
    val isNetheriteEnchant: Boolean
)

data class EnchantDetailPopupTargetStates(
    val target: Enchantment?,
    val isUnset: Boolean
)

@Composable
fun ArmorPropertyPopup() {
    val _target by remember { derivedStateOf { arctic.armorProperties.detailTarget } }
    val _into by remember { derivedStateOf { arctic.armorProperties.createInto } }
    val _created  by remember { derivedStateOf { arctic.armorProperties.created } }

    val _hasTarget = arctic.armorProperties.hasDetailTarget
    val _hasInto = arctic.armorProperties.hasCreateInto

    val collectionTargetState =
        if (_hasInto) {
            ArmorPropertyCollectionPopupTargetStates(
                holder = _into,
                index = _into?.armorProperties?.size
            )
        } else {
            val index = _target?.holder?.armorProperties?.indexOf(_target)
            ArmorPropertyCollectionPopupTargetStates(
                holder = _target?.holder,
                index = index
            )
        }

    val detailTargetState = ArmorPropertyDetailPopupTargetStates(
        holder = collectionTargetState.holder,
        target = _target,
        created = _created
    )

    val detailSizeTransformDuration: (ArmorPropertyDetailPopupTargetStates, ArmorPropertyDetailPopupTargetStates) -> Int = { initial, target ->
        if ((initial.created && !target.created) || (initial.holder == null && initial.target == null && target.created)) 0
        else 250
    }

    Backdrop(_hasTarget || _hasInto) {
        if (_hasTarget) arctic.armorProperties.closeDetail()
        else if (_hasInto) arctic.armorProperties.cancelCreation()
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedCollection(collectionTargetState) { (holder, index) ->
            val indexValid = index != null && index < (holder?.armorProperties?.size ?: 0)

            if (holder != null) ArmorPropertyCollection(holder, index, indexValid)
            else Box(modifier = Modifier.width(700.dp).fillMaxHeight())
        }

        AnimatedDetail(detailTargetState, sizeTransformDuration = detailSizeTransformDuration) { (_, target, created) ->
            if (target != null) ArmorPropertyDetail(target)
            else Box(modifier = Modifier.size(0.dp, 500.dp))

            if (target == null && created) {
                Box(modifier = Modifier.width(675.dp).height(500.dp))
            } else if (target == null) {
                Box(modifier = Modifier.width(0.dp).height(500.dp))
            }
        }
    }
}

data class ArmorPropertyCollectionPopupTargetStates(
    val holder: Item?,
    val index: Int?
)

data class ArmorPropertyDetailPopupTargetStates(
    val holder: Item?,
    val target: ArmorProperty?,
    val created: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Backdrop(visible: Boolean, onClick: () -> Unit) =
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), label = "Backdrop") {
        Box(modifier = Modifier.fillMaxSize().background(Color(0x60000000)).onClick(onClick = onClick))
    }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> AnimatedCollection(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn() + slideIn(initialOffset = { IntOffset(- it.width / 10, 0) })
            val exit = fadeOut(tween(durationMillis = 100))
            enter with exit
        },
        modifier = Modifier.width(750.dp),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> AnimatedDetail(targetState: S, sizeTransformDuration: (S, S) -> Int = { _, _, -> 250 }, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn(tween(durationMillis = 250)) + slideIn(tween(durationMillis = 250), initialOffset = { IntOffset(0, 50) })
            val exit = fadeOut(tween(durationMillis = 250)) + slideOut(tween(durationMillis = 250), targetOffset = { IntOffset(0, -50) })
            enter with exit using SizeTransform(false) { _, _ -> tween(durationMillis = sizeTransformDuration(this.initialState, this.targetState)) }
        },
        modifier = Modifier.height(500.dp),
        content = content
    )
