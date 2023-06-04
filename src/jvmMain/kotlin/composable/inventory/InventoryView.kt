package composable.inventory

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.states.Item
import arctic.states.arctic

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RowScope.InventoryView() {
    Debugging.recomposition("InventoryView")

    AnimatedContent(
        targetState = arctic.stored,
        transitionSpec = { (fadeIn() + slideIn(initialOffset = { IntOffset(0, -50.dp.value.toInt()) }) with fadeOut() + slideOut(targetOffset = { IntOffset(0, -50.dp.value.toInt()) })) using SizeTransform(false) }
    ) { stored ->
        if (stored != null) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                AnimatedContent(
                    targetState = stored to arctic.view,
                    transitionSpec = {
                        val enter = fadeIn(tween(durationMillis = 250)) + slideIn(tween(durationMillis = 250), initialOffset = { IntOffset(50, 0) })
                        val exit = fadeOut(tween(durationMillis = 250)) + slideOut(tween(durationMillis = 250), targetOffset = { IntOffset(-50, 0) })
                        enter with exit using SizeTransform(false) { _, _ -> tween(durationMillis = 250) }
                    },
                    modifier = Modifier.width(657.dp)
                ) { (stored, view) ->
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (view == "inventory") {
                            LeftArea {
                                EquippedItems(stored.equippedItems)
                                Divider()
                                InventoryItems(stored.unequippedItems)
                            }
                        } else if (view == "storage") {
                            LeftArea { InventoryItems(stored.storageChestItems) }
                        }
                    }
                }
                RightArea {
                    AnimatorBySelectedItemExists(arctic.selection.selected.any { item -> item != null }) {
                        if (it) ItemComparatorView(arctic.selection.selected)
                        else TitleView()
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun LeftArea(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxHeight().padding(top = 20.dp),
        content = content
    )

@Composable
private fun RowScope.RightArea(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxHeight().width(718.dp),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S> AnimatorBySelectedItemExists(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter with exit
        },
        content = content
    )

@Composable
private fun ItemComparatorView(items: List<Item?>) {
    Debugging.recomposition("ItemComparatorView")

    val scroll = rememberScrollState()
    val adapter = rememberScrollbarAdapter(scroll)
    Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp, bottom = 40.dp, start = 75.dp)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll), verticalArrangement = Arrangement.Center) {
            AnimatedItemDetailView(items[0])
            Spacer(modifier = Modifier.height(20.dp))
            AnimatedItemDetailView(items[1])
        }
        VerticalScrollbar(
            adapter = adapter,
            style = GlobalScrollbarStyle,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 20.dp)
        )
    }
}

val GlobalScrollbarStyle = ScrollbarStyle(
    thickness = 20.dp,
    minimalHeight = 100.dp,
    hoverColor = Color.White.copy(alpha = 0.3f),
    unhoverColor = Color.White.copy(alpha = 0.15f),
    hoverDurationMillis = 0,
    shape = RoundedCornerShape(3.dp),
)

private val JetbrainsMono = FontFamily(
    Font (
        resource = "JetBrainsMono-Regular.ttf",
        weight = FontWeight.W400,
        style = FontStyle.Normal
    )
)

@Composable
private fun TitleView() =
    Column(
        modifier = Modifier.fillMaxSize().offset(x = 35.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

    }

@Composable
fun ReadmeContainer(content: @Composable ColumnScope.() -> Unit) =
    Column(modifier = Modifier.fillMaxWidth().padding(start = 70.dp, end = 50.dp, bottom = 60.dp), content = content)

@Composable
private fun H3(text: String) =
    Text(text = text, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 10.dp, top = 40.dp))

@Composable
private fun H4(text: String) =
    Text(text = text, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 10.dp))

@Composable
private fun Paragraph(text: AnnotatedString) =
    Text(text = text, fontSize = 20.sp, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(bottom = 20.dp))

@Composable
private fun Paragraph(text: String) =
    Text(text = text, fontSize = 20.sp, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(bottom = 20.dp))

@Composable
fun TitleText(color: Color, offset: Dp) =
    Text(
        text = "Dungeons",
        fontFamily = JetbrainsMono,
        color = color,
        fontSize = 125.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.offset(offset, offset)
    )
