import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import blackstone.states.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.*
import blackstone.states.ArcticStates
import blackstone.states.StoredDataState
import blackstone.states.sp
import composable.BottomBar
import composable.Popups
import composable.Selector
import composable.inventory.InventoryView
import extensions.GameResources
import io.StoredFile.Companion.readAsStoredFile
import utils.openURL

val arctic = ArcticStates()

@Composable
@Preview
fun App() {
    Debugging.recomposition("App")

    val backdropVisible =
        arctic.dialogs.fileSaveDstSelector ||
        arctic.enchantments.hasDetailTarget ||
        arctic.armorProperties.hasDetailTarget ||
        arctic.armorProperties.hasCreateInto ||
        arctic.creation.enabled ||
        arctic.alerts.inventoryFull ||
        arctic.alerts.closeFile ||
        arctic.alerts.fileLoadFailed != null ||
        arctic.edition.target != null ||
        arctic.deletion.target != null ||
        arctic.duplication.target != null
    val moreBlur = arctic.creation.target != null

    val popupBackdropBlurRadius by animateDpAsState(if (moreBlur) 100.dp else if (backdropVisible) 50.dp else 0.dp, tween(durationMillis = 250))

    AppRoot {
        Background()
        MainContainer(popupBackdropBlurRadius) {
            ContentContainer {
                InventoryView()
            }
            BottomBarContainer { stored -> BottomBar(stored) }
        }
        Popups()
    }
}

@Composable
fun AppRoot(content: @Composable BoxScope.() -> Unit) =
    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff272727)),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Background() =
    AnimatedContent(
        targetState = arctic.stored == null,
        transitionSpec = {
            val enter = fadeIn()
            val exit = fadeOut()
            enter with exit
        },
        modifier = Modifier.fillMaxSize()
    ) {
        if (it) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = GameResources.image { "/Game/UI/Materials/LoadingScreens/Loading_Ancient_Hunt.png" },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(50.dp),
                    contentDescription = null
                )
                Box(
                    modifier = Modifier
                        .size(width = 690.dp, height = 1060.dp)
                        .background(Color(0xae27241f), shape = RoundedCornerShape(12.5.dp))
                        .border(2.dp, Color(0xae3e3933), shape = RoundedCornerShape(12.5.dp))
                )
                NonExpandingTitleViewSection(modifier = Modifier.offset(x = (-492.5).dp, y = (-582.5).dp)) {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0x80ffffff))) { append("version: ") }
                            append("activator_rail")
                            withStyle(SpanStyle(color = Color(0x80ffffff))) { append(", compatible with ") }
                            append("Minecraft Dungeons 1.17.0.0")
                        },
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
                Row(modifier = Modifier.fillMaxWidth().height(1060.dp).padding(horizontal = 40.dp)) {
                    TitleViewSideColumn {
                        TitleViewSection {
                            Box(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(topStart = 12.5.dp, topEnd = 12.5.dp))) {
                                Text(
                                    "Changelogs",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp).background(Color(0xae3e3933)))
                            Text(
                                buildAnnotatedString { append("이것이 초기 버전입니다."); withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = Color.White.copy(alpha = 0.3f))) { append("과연 다음 버전이 있을까") } },
                                textAlign = TextAlign.Center,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 18.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        TitleViewSection(modifier = Modifier.height(470.dp)) {
                            Box(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(topStart = 12.5.dp, topEnd = 12.5.dp))) {
                                Text(
                                    "Applied commits in this build",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp).background(Color(0xae3e3933)))
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                for (commit in GitCommits.current.slice(0 until 20)) {
                                    CommitRow(commit.subject, commit.abbreviatedCommit, commit.author.date)
                                }
                                Text(
                                    "이외에도 더 많은 커밋이 있지만\n여백이 부족하여 가려졌습니다.",
                                    textAlign = TextAlign.Center,
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 18.sp,
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp, bottom = 20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TitleViewSection {
                            Box(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(topStart = 12.5.dp, topEnd = 12.5.dp))) {
                                Text(
                                    "External links",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp).background(Color(0xae3e3933)))
                            Spacer(modifier = Modifier.height(15.dp))
                            LinkText(
                                "저장소: ",
                                "https://github.com/hoonkun/dungeons-editor",
                                "github.com/hoonkun/dungeons-editor"
                            )
                            LinkText(
                                "만든 사람 (GitHub): ",
                                "https://github.com/hoonkun",
                                "github.com/hoonkun"
                            )
                            Spacer(modifier = Modifier.height(15.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(690.dp))
                    TitleViewSideColumn {
                        Spacer(modifier = Modifier.weight(1f))
                        TitleViewSection {
                            Box(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(topStart = 12.5.dp, topEnd = 12.5.dp))) {
                                Text(
                                    "Amateur Tips",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp).background(Color(0xae3e3933)))
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp)) {
                                LI("파일 선택기는 직접 입력하기보다는 기존 탐색기에서 경로를 복사해 붙혀넣는 것이 편리합니다.")
                                LI("우측 영역에는 좌클릭/우클릭으로 최대 두 개의 아이템을 표시할 수 있습니다.")
                                LI("활성화된 효과부여 슬롯에서 나머지 비활성화된 슬롯을 수정하려면 먼저 활성화된 효과부여를 0레벨로 변경하여 비활성화합니다.")
                                LI("이미 추가된 효과, 방어구 프로퍼티를 삭제하려면 목록에서 선택된 항목을 다시 한 번 누릅니다.")
                                LI("대체로, 닫기 버튼이 없는 팝업 화면에서 빠져나가려면 주변의 빈 공간을 누르면 됩니다.")
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }

@Composable
fun LI(text: String) =
    Text(text, color = Color.White, fontSize = 20.sp, lineHeight = 30.sp, modifier = Modifier.padding(vertical = 10.dp))

@Composable
fun LinkText(suffix: String, url: String, displayLink: String = url) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    Text(
        buildAnnotatedString {
            append(suffix)
            withStyle(SpanStyle(textDecoration = if (hovered) TextDecoration.Underline else TextDecoration.None, color = Color(0xffffca9c))) {
                append(displayLink)
            }
        },
        color = Color.White,
        fontSize = 18.sp,
        modifier = Modifier.padding(vertical = 5.dp, horizontal = 20.dp).hoverable(source).clickable(source, null) { openURL(url) }
    )
}

@Composable
fun CommitRow(message: String, hash: String, committedAt: String) =
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.align(Alignment.CenterEnd).fillMaxWidth().padding(vertical = 20.dp, horizontal = 20.dp)) {
            Text(text = message, fontSize = 16.sp, color = Color.White, overflow = TextOverflow.Ellipsis, maxLines = 1, modifier = Modifier.width(280.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(text = hash, fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.width(55.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = committedAt, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.width(85.dp))
        }
        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xae3e3933)))
    }

@Composable
fun RowScope.TitleViewSideColumn(content: @Composable ColumnScope.() -> Unit) =
    Column (
        modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
        content = content
    )

@Composable
fun TitleViewSection(modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xae27241f), shape = RoundedCornerShape(12.5.dp))
            .border(2.dp, Color(0xae3e3933), shape = RoundedCornerShape(12.5.dp))
            .then(modifier)
    ) {
        content()
    }
@Composable
fun NonExpandingTitleViewSection(modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    Column(
        modifier = modifier
            .background(Color(0xae27241f), shape = RoundedCornerShape(12.5.dp))
            .border(2.dp, Color(0xae3e3933), shape = RoundedCornerShape(12.5.dp))
            .padding(vertical = 20.dp, horizontal = 30.dp)
    ) {
        content()
    }

@Composable
fun BoxScope.MainContainer(blurRadius: Dp, content: @Composable ColumnScope.() -> Unit) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().blur(blurRadius),
        content = content
    )

@Composable
fun ColumnScope.ContentContainer(content: @Composable RowScope.() -> Unit) =
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.weight(1f).fillMaxWidth(0.7638888f).offset(x = (-35).dp),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomBarContainer(content: @Composable BoxScope.(StoredDataState) -> Unit) =
    AnimatedContent(
        targetState = arctic.stored,
        transitionSpec = {
            val enter = fadeIn(tween(250)) + slideIn(tween(250), initialOffset = { IntOffset(0, 20.dp.value.toInt()) })
            val exit = fadeOut(tween(250)) + slideOut(tween(250), targetOffset = { IntOffset(0, 20.dp.value.toInt()) })
            enter with exit using SizeTransform(false) { _, _ -> tween(durationMillis = 250) }
        },
        modifier = Modifier.fillMaxWidth(),
        content = {
            if (it != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(85.dp),
                    content = { content(it) }
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth())
            }
        }
    )

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(1800.dp, 1400.dp), position = WindowPosition(Alignment.Center)),
        resizable = false
    ) {
        App()
    }
    if (arctic.dialogs.fileSaveDstSelector) {
        Window(onCloseRequest = { arctic.dialogs.fileSaveDstSelector = false }, state = rememberWindowState(size = DpSize(1050.dp, 620.dp)), resizable = false) {
            Selector(
                selectText = "저장",
                validator = { !it.isDirectory },
                onSelect = { arctic.requireStored.save(it); arctic.dialogs.fileSaveDstSelector = false }
            )
        }
    }
    if (arctic.dialogs.fileLoadSrcSelector) {
        Window(onCloseRequest = { arctic.dialogs.fileLoadSrcSelector = false }, state = rememberWindowState(size = DpSize(1050.dp, 620.dp)), resizable = false) {
            Selector(
                selectText = "열기",
                validator = { !it.isDirectory && it.isFile },
                onSelect = {
                    try {
                        arctic.view = "inventory"
                        arctic.stored = StoredDataState(it.readAsStoredFile().root)
                    } catch (e: Exception) {
                        arctic.alerts.fileLoadFailed = e.message
                    } finally {
                        arctic.dialogs.fileLoadSrcSelector = false
                    }
                }
            )
        }
    }
}
