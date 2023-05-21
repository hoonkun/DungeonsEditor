package composable

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composable.popup.EnchantmentDetailView
import editorState

@Composable
fun PopupBox() {

    PopupBoxAnimator(editorState.detailState.selectedEnchantment) {
        if (it == null) return@PopupBoxAnimator

        PopupBoxRoot(size = 675.dp to 300.dp) {
            EnchantmentDetailView(it, requestClose =  { editorState.detailState.unselectEnchantment() })
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
fun PopupButton(text: String, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val pressed by source.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .hoverable(source)
            .clickable(source, null, onClick = onClick)
            .drawBehind {
                val color =
                    if (pressed) Color(0xffd07039)
                    else if (hovered) Color(0xffffa74f)
                    else Color(0xffff8a46)

                drawRoundRect(color, cornerRadius = CornerRadius(5.dp.value))
            }
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 20.sp, color = Color.White),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 30.dp)
        )
    }
}

@Composable
fun PopupCloseButton(onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    Box (
        modifier = Modifier
            .size(50.dp)
            .offset(10.dp, 10.dp)
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