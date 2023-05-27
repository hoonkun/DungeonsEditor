package composable

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import arctic
import composable.popup.ArmorPropertyDetailView
import composable.popup.ArmorPropertySelectorView
import composable.popup.EnchantmentDetailView
import composable.popup.EnchantmentSelectorView

@Composable
fun PopupBox() {
    Debugging.recomposition("PopupBox")

    EnchantmentDetailPopup()
    ArmorPropertyDetailPopup()
}

@Composable
fun EnchantmentDetailPopup() {
    Debugging.recomposition("EnchantmentDetailPopup")

    val selectedEnchantment = arctic.enchantments.detailTarget
    val selectedEnchantmentHolderVisible = arctic.items.selected(selectedEnchantment?.holder)

    val selectorOpen = arctic.enchantments.hasDetailTarget

    PopupBoxAnimator(Triple(selectorOpen, selectedEnchantment, selectedEnchantmentHolderVisible)) { (open, selected, holderVisible) ->
        if (!open || selected == null || !holderVisible) return@PopupBoxAnimator

        PopupBoxRoot(size = 675.dp to 800.dp, offset = 100.dp to (-320).dp) {
            EnchantmentSelectorView(selected.holder, selected)
        }
    }

    PopupBoxAnimator(selectedEnchantment to selectedEnchantmentHolderVisible) {
        val enchantment = it.first
        val open = it.second

        if (enchantment == null || !open) return@PopupBoxAnimator

        PopupBoxRoot(size = 675.dp to 300.dp) {
            EnchantmentDetailView(enchantment, requestClose = { arctic.enchantments.closeDetail() })
        }
    }
}

@Composable
fun ArmorPropertyDetailPopup() {
    Debugging.recomposition("ArmorPropertyDetailPopup")

    val selected = arctic.armorProperties.detailTarget
    val holderVisible = arctic.items.selected(selected?.holder)

    val open = arctic.armorProperties.hasDetailTarget

    PopupBoxAnimator(Triple(open, selected, holderVisible)) { (open, selected, holderVisible) ->
        if (!open || selected == null || !holderVisible) return@PopupBoxAnimator

        PopupBoxRoot(size = 675.dp to 800.dp, offset = 100.dp to (-160).dp) {
            ArmorPropertySelectorView(selected)
        }
    }

    PopupBoxAnimator(selected to holderVisible) { (property, open) ->
        if (property == null || !open) return@PopupBoxAnimator

        PopupBoxRoot(size = 675.dp to 140.dp) {
            ArmorPropertyDetailView(property, requestClose = { arctic.armorProperties.closeDetail() })
        }
    }
}

@Composable
private fun PopupBoxRoot(
    offset: Pair<Dp, Dp> = 100.dp to 0.dp,
    size: Pair<Dp, Dp> = 675.dp to 1000.dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Debugging.recomposition("PopupBoxRoot")

    val source = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .offset(offset.first, offset.second)
            .width(size.first).height(size.second)
            .background(Color(0xff191919))
            .clickable(source, null) { }
            .then(modifier),
        content = content
    )
}

@Composable
fun PopupCloseButton(onClick: () -> Unit) {
    Debugging.recomposition("PopupCloseButton")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    Box (
        modifier = Modifier
            .size(50.dp)
            .offset(x = 10.dp, y = (-10).dp)
            .hoverable(source)
            .clickable(source, null, onClick = onClick)
            .drawBehind {
                drawRect(
                    if (hovered) Color.White else Color(0xff79706b),
                    topLeft = Offset(12f, this.size.height / 2 - 2f),
                    size = Size(this.size.width - 24f, 4f)
                )
            }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S> PopupBoxAnimator(targetState: S, alignment: Alignment = Alignment.BottomStart, content: @Composable BoxScope.(state: S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val slideSpec = tween<IntOffset>(durationMillis = 250)
            val fadeSpec = tween<Float>(durationMillis = 250)
            val enter = slideInVertically(initialOffsetY = { it / 10 }, animationSpec = slideSpec) + fadeIn(fadeSpec)
            val exit = slideOutVertically(targetOffsetY = { it / 10 }, animationSpec = slideSpec) + fadeOut(fadeSpec)
            enter with exit
        }
    ) {
        Box(
            contentAlignment = alignment,
            modifier = Modifier.fillMaxSize(),
        ) {
            content(it)
        }
    }