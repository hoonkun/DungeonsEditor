package composable

import ItemData
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arctic
import blackstone.states.ArmorProperty
import blackstone.states.Enchantment
import blackstone.states.Item
import composable.blackstone.popup.*

@Composable
fun BoxScope.Popups() {
    Debugging.recomposition("Popups")

    EnchantmentPopup()
    ArmorPropertyPopup()

    ItemCreationPopup()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ItemCreationPopup() {

    val _enabled: Boolean = arctic.itemCreation.enabled
    val _target: ItemData? = arctic.itemCreation.target

    val _variant: String = arctic.itemCreation.filter

    val blurRadius by animateDpAsState(if (_target != null) 75.dp else 0.dp)

    Backdrop(arctic.itemCreation.enabled) { arctic.itemCreation.disable() }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().blur(blurRadius)
    ) {
        Box {
            AnimatedCollection(_enabled, width = 160.dp, modifier = Modifier.offset(x = (-120).dp)) {
                if (it) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.requiredWidth(160.dp).padding(top = 54.dp)) {
                        VariantFilterTextInteractable("근거리", "Melee")
                        VariantFilterTextInteractable("원거리", "Ranged")
                        VariantFilterTextInteractable("방어구", "Armor")
                        VariantFilterTextInteractable("유물", "Artifact")
                    }
                } else {
                    Spacer(modifier = Modifier.requiredWidth(160.dp).fillMaxHeight())
                }
            }
            AnimatedContent(
                targetState = _enabled to _variant,
                transitionSpec = {
                    val enter = fadeIn(tween(durationMillis = 250)) + slideIn(tween(durationMillis = 250), initialOffset = { IntOffset(- 70.dp.value.toInt(), 0) })
                    var exit = fadeOut(tween(durationMillis = 250))
                    if (targetState.first) exit += scaleOut(tween(durationMillis = 250), targetScale = 0.9f)
                    enter with exit
                },
                modifier = Modifier.width(1050.dp)
            ) { (enabled, variant) ->
                if (enabled) ItemDataCollection(variant) { arctic.itemCreation.target = it }
                else Box(modifier = Modifier.width(950.dp).fillMaxHeight())
            }
        }
    }

    Backdrop(_target != null) { arctic.itemCreation.target = null }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedDetail(_target, modifier = Modifier.fillMaxWidth().height(550.dp)) { target ->
            if (target != null) ItemDataDetail(target)
            else Box(modifier = Modifier.fillMaxSize())
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VariantFilterTextInteractable(text: String, variant: String) {

    val selected = arctic.itemCreation.filter == variant
    val onClick = { arctic.itemCreation.filter = variant }

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    val blurAlpha by animateFloatAsState(if (hovered) 0.8f else 0f)
    val overlayAlpha by animateFloatAsState(if (selected) 1f else 0.6f)

    Box(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .hoverable(source)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary), onClick = onClick)
    ) {
        VariantFilterText(text, modifier = Modifier.blur(10.dp).alpha(blurAlpha))
        VariantFilterText(text, modifier = Modifier.alpha(overlayAlpha))
    }
}

@Composable
fun VariantFilterText(text: String, modifier: Modifier = Modifier) =
    Text(
        text = text,
        color = Color.White,
        fontSize = 35.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 20.dp, top = 5.dp, bottom = 5.dp, end = 20.dp)
    )

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

    val slideEnabled: (EnchantDetailPopupTargetStates, EnchantDetailPopupTargetStates) -> Boolean = { initial, target ->
        !initial.isUnset && target.target != null && !target.isUnset || initial.target == null && target.target != null
    }

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
        AnimatedDetail(detailTargetState, slideEnabled = slideEnabled) { (target, isUnset) ->
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
        if ((initial.holder == null && initial.created && !target.created) || (initial.holder == null && initial.target == null && target.created)) 0
        else 250
    }

    val slideEnabled: (ArmorPropertyDetailPopupTargetStates, ArmorPropertyDetailPopupTargetStates) -> Boolean = { initial, target ->
        initial.target != null && target.holder != null && target.target != null || initial.holder == null && target.holder != null
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

        AnimatedDetail(detailTargetState, slideEnabled = slideEnabled, sizeTransformDuration = detailSizeTransformDuration) { (_, target, created) ->
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
fun <S> AnimatedCollection(targetState: S, width: Dp = 750.dp, modifier: Modifier = Modifier, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn(tween(durationMillis = 250)) + slideIn(tween(durationMillis = 250), initialOffset = { IntOffset(- 70.dp.value.toInt(), 0) })
            val exit = fadeOut(tween(durationMillis = 250))
            enter with exit
        },
        modifier = modifier.then(Modifier.width(width)),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> AnimatedDetail(
    targetState: S,
    sizeTransformDuration: (S, S) -> Int = { _, _, -> 250 },
    slideEnabled: (S, S) -> Boolean = { _, _ -> true },
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(S) -> Unit
) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val slide = slideEnabled(this.initialState, this.targetState)

            var enter = fadeIn(tween(durationMillis = 250))
            if (slide) enter += slideIn(tween(durationMillis = 250), initialOffset = { IntOffset(0, 50) })

            var exit = fadeOut(tween(durationMillis = 250))
            if (slide) exit += slideOut(tween(durationMillis = 250), targetOffset = { IntOffset(0, -50) })

            enter with exit using SizeTransform(false) { _, _ -> tween(durationMillis = sizeTransformDuration(this.initialState, this.targetState)) }
        },
        modifier = modifier.then(Modifier.height(500.dp)),
        content = content
    )
