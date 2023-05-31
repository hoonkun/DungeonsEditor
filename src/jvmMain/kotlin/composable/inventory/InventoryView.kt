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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import blackstone.states.dp
import blackstone.states.sp
import arctic
import blackstone.states.items.unequipped
import blackstone.states.Item
import blackstone.states.items.equippedItems
import composable.RetroButton

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RowScope.InventoryView() {
    Debugging.recomposition("InventoryView")

    AnimatedContent(arctic.stored) { stored ->
        if (stored != null) {
            AnimatedContent(
                targetState = stored to arctic.view,
                transitionSpec = {
                    val enter = fadeIn(tween(durationMillis = 250)) + slideIn(
                        tween(durationMillis = 250),
                        initialOffset = { IntOffset(50, 0) })
                    val exit = fadeOut(tween(durationMillis = 250)) + slideOut(
                        tween(durationMillis = 250),
                        targetOffset = { IntOffset(-50, 0) })
                    enter with exit using SizeTransform(false) { _, _ -> tween(durationMillis = 250) }
                },
                modifier = Modifier.width(657.dp)
            ) { (stored, view) ->
                Row(modifier = Modifier.fillMaxSize()) {
                    if (view == "inventory") {
                        LeftArea {
                            EquippedItems(stored.equippedItems)
                            Divider()
                            InventoryItems(stored.items.filter(unequipped))
                        }
                    } else if (view == "storage") {
                        LeftArea { InventoryItems(stored.storageChestItems) }
                    }
                }
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
        Box(contentAlignment = Alignment.Center, modifier = Modifier.offset(x = (-5).dp)) {
            TitleText(Color(0xffec691f), 8.dp)
            TitleText(Color(0xddffbb33), 0.dp)
            Text(
                text = "editor",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 35.sp,
                fontFamily = JetbrainsMono,
                modifier = Modifier.offset(x = 70.dp, y = (80).dp)
            )
//            Text(
//                text = "Minecraft Dungeons 1.17.0.0 버전과 호환",
//                fontSize = 20.sp,
//                color = Color.White.copy(alpha = 0.45f),
//                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-142.5).dp, y = 20.dp)
//            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        ReadmeContainer {
            H3("README.md")
            Paragraph("Minecraft Dungeons 의 세이브 파일 일부를 수정합니다.")
            H4("형식적인 문장")
            Paragraph(
                buildAnnotatedString {
                    append("어디까지나 연구용일 뿐입니다. 데미지 계산이라던지... \n뭐 그런 것들 말입니다. ")
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = Color.White.copy(0.6f))) {
                        append("아무도 그렇게 안씀")
                    }
                }
            )
            Paragraph("파일 선택을 시도하면 굉장히 불친절한 탐색기가 나올텐데, 그것만 어찌 잘 넘어가시면 그 이후는 편리하게 쓸 수 있도록 최선을 다했습니다.")
            H4("최소한의 주의사항")
            Paragraph(
                buildAnnotatedString {
                    append("이 툴로 편집한 이후 탄생한 무언가가 정상적인 데이터라고 보장할 수 없으므로, ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("수정을 가하시기 전 항상 백업을 따로 만들어주세요")
                    }
                    append(".")
                }
            )
            H4("아무도 궁금해하지 않는 사항")
            Paragraph("HoonKun(GitHub) 이 만들었습니다. 심심해서요.\n절대 던전스 데이터 날려먹고 확률망겜에 지쳐서 만든 툴이 아닙니다.")
        }
        RetroButton(
            text = if (arctic.stored == null) "파일 선택" else "다른 파일 선택",
            color = Color(0xff3f8e4f),
            hoverInteraction = "outline",
            modifier = Modifier.align(Alignment.End).offset(x = (-50).dp)
        ) {
            arctic.dialogs.fileLoadSrcSelector = true
        }
        Spacer(modifier = Modifier.height(20.dp))
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
