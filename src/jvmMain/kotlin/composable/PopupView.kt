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
fun BoxScope.EnchantmentPopup() {
    val detailTarget by remember { derivedStateOf { arctic.enchantments.detailTarget } }
    val rootEnabled by remember { derivedStateOf { arctic.enchantments.hasDetailTarget } }

    val detailEnabled by remember { derivedStateOf { arctic.enchantments.detailTarget.let { it != null && it.id != "Unset" } } }

    Backdrop(rootEnabled) { arctic.enchantments.closeDetail() }

    Row(modifier = Modifier.fillMaxWidth()) {
        LeftSlide(EnchantPopupLeftState(rootEnabled, detailTarget)) { (visible, target) ->
            if (!visible || target == null) Box(modifier = Modifier.width(700.dp).fillMaxHeight().align(Alignment.CenterEnd))
            else EnchantmentsCollection(target)
        }

        RightSlide(EnchantPopupRightState(rootEnabled && detailEnabled, detailTarget)) { (visible, target) ->
            if (!visible || target == null) Box(modifier = Modifier.size(675.dp, 300.dp))
            else EnchantmentDetail(target)
        }
    }
}

data class EnchantPopupLeftState(
    val visible: Boolean,
    val target: Enchantment?
)

data class EnchantPopupRightState(
    val visible: Boolean,
    val target: Enchantment?
)

@Composable
fun BoxScope.ArmorPropertyPopup() {
    val detailTarget by remember { derivedStateOf { arctic.armorProperties.detailTarget } }
    val rootEnabled by remember { derivedStateOf { arctic.armorProperties.hasDetailTarget } }

    val detailEnabled by remember { derivedStateOf { arctic.armorProperties.detailTarget.let { it != null } } }

    Backdrop(rootEnabled) { arctic.armorProperties.closeDetail() }

    Row(modifier = Modifier.fillMaxWidth()) {
        LeftSlide(ArmorPropertyPopupLeftState(rootEnabled, detailTarget)) { (visible, target) ->
            if (!visible) Box(modifier = Modifier.width(700.dp).fillMaxHeight().align(Alignment.CenterEnd))
            else ArmorPropertyCollection(target)
        }

        RightSlide(ArmorPropertyPopupRightState(rootEnabled && detailEnabled, detailTarget)) { (visible, target) ->
            if (!visible || target == null) Box(modifier = Modifier.size(675.dp, 300.dp))
            else ArmorPropertyDetail(target)
        }
    }
}

data class ArmorPropertyPopupLeftState(
    val visible: Boolean,
    val target: ArmorProperty?
)

data class ArmorPropertyPopupRightState(
    val visible: Boolean,
    val target: ArmorProperty?
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoxScope.Backdrop(visible: Boolean, onClick: () -> Unit) =
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), label = "Backdrop") {
        Box(modifier = Modifier.fillMaxSize().background(Color(0x60000000)).onClick(onClick = onClick))
    }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> RowScope.LeftSlide(targetState: S, content: @Composable BoxScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn() + slideIn(initialOffset = { IntOffset(-it.width / 10, 0) })
            val exit = fadeOut(tween(durationMillis = 100))
            enter with exit
        },
        modifier = Modifier.weight(0.5f),
        content = { Box(modifier = Modifier.fillMaxSize()) { content(it) } }
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> RowScope.RightSlide(targetState: S, content: @Composable BoxScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn() + slideIn(initialOffset = { IntOffset(it.width / 10, 0) })
            val exit = fadeOut(tween(durationMillis = 100))
            enter with exit
        },
        modifier = Modifier.weight(0.53f),
        content = { Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxSize()) { content(it) } }
    )
