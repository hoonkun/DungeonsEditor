import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.*
import arctic.states.DungeonsJsonState
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import blackstone.states.*
import composable.BottomBar
import composable.Popups
import composable.Selector
import composable.inventory.InventoryView
import dungeons.IngameImages
import dungeons.readDungeonsJson

@Composable
@Preview
fun App() {
    Debugging.recomposition("App")

    val backdropVisible = arctic.backdropBlur
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun Background() =
    AnimatedContent(
        targetState = arctic.stored == null,
        transitionSpec = { fadeIn() with fadeOut() },
        modifier = Modifier.fillMaxSize()
    ) { isInTitle ->
        if (isInTitle) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    bitmap = IngameImages.get { "/Game/UI/Materials/LoadingScreens/Loading_Ancient_Hunt.png" },
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(50.dp)
                        .drawWithContent {
                            drawContent()
                            drawRect(Color.Black.copy(alpha = 0.6f))
                        }
                )
                ArrowDecor(modifier = Modifier.align(Alignment.CenterEnd).offset(x = (500).dp, y = -650.dp))
                ArrowDecor(modifier = Modifier.align(Alignment.CenterStart).offset(x = (-300).dp, y = 425.dp))
                Go(color = Color(0x55ff8800), modifier = Modifier.blur(125.dp))
                Go(color = Color(0xff090500), modifier = Modifier.onClick { arctic.dialogs.fileLoadSrcSelector = true })
                GoOverlay(
                    text = "Getting Started",
                    color = Color(0xff090500),
                    modifier = Modifier
                        .offset(x = (-295).dp, y = (-260).dp)
                        .background(Color(0xffff8800))
                        .padding(start = 15.dp, top = 5.dp, bottom = 5.dp, end = 415.dp)
                )
                Divider(alignment = Alignment.Top)
                Divider(alignment = Alignment.Bottom)
                BottomOverlayGradient()
                BottomEndDescriptions {
                    BottomEndDescription("Dungeons Editor, 1.0.0 by HoonKun")
                    BottomEndDescription("Compatible with Minecraft Dungeons 1.17.0.0")
                }
                Column(modifier = Modifier.align(Alignment.TopStart).fillMaxHeight().offset(x = 100.dp), verticalArrangement = Arrangement.Center) {
                    SelectSectionHeader("main_icon_history.svg", "Recent Files")
                    Spacer(modifier = Modifier.height(75.dp))
                    SelectCandidateText(text = "6762E9EB4A8B0A1ECF7989917F3E5366.dat", subtext = "~/minecraft/DungeonsData/SaveData")
                    SelectCandidateText(text = "AC9E38AD4D442E3EFD5BFC9177A2EBB9.dat", subtext = "~/minecraft/DungeonsData/SaveData")
                    Spacer(modifier = Modifier.height(125.dp))
                    SelectSectionHeader("main_icon_detected_files.svg", "Detected Files")
                    Spacer(modifier = Modifier.height(75.dp))
                    SelectCandidateText(text = "6762E9EB4A8B0A1ECF7989917F3E5366.dat", subtext = "~/minecraft/DungeonsData/SaveData")
                    Spacer(modifier = Modifier.height(75.dp))
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }

@Composable
fun BoxScope.Divider(alignment: Alignment.Vertical) =
    Spacer(
        modifier = Modifier
            .size(50.dp, 1.dp)
            .align(if (alignment == Alignment.Top) Alignment.TopCenter else Alignment.BottomCenter)
            .offset(y = if (alignment == Alignment.Top) 125.dp else (-125).dp)
            .background(Color.White.copy(alpha = 0.25f))
    )

@Composable
fun BoxScope.ArrowDecor(modifier: Modifier = Modifier) =
    Text(
        text = "->",
        fontSize = 1000.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xff090500),
        textAlign = TextAlign.Start,
        fontFamily = FontFamily(Font(resource = "JetBrainsMono-Regular.ttf", weight = FontWeight.W400, style = FontStyle.Normal)),
        modifier = Modifier.fillMaxWidth().scale(1.2f).alpha(0.7f).then(modifier)
    )

@Composable
fun BoxScope.Go(color: Color, modifier: Modifier = Modifier) =
    Text(
        text = "GO",
        fontSize = 1000.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        letterSpacing = (-50).sp,
        textAlign = TextAlign.End,
        fontFamily = FontFamily(Font(resource = "JetBrainsMono-Regular.ttf", weight = FontWeight.W400, style = FontStyle.Normal)),
        modifier = Modifier.fillMaxWidth().align(Alignment.CenterEnd).scale(1.2f).offset(x = (-30).dp).then(modifier)
    )

@Composable
fun BoxScope.GoOverlay(text: String, color: Color, modifier: Modifier = Modifier) =
    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        fontFamily = FontFamily(Font(resource = "JetBrainsMono-Regular.ttf", weight = FontWeight.W400, style = FontStyle.Normal)),
        modifier = Modifier.align(Alignment.CenterEnd).then(modifier)
    )

@Composable
fun BoxScope.BottomOverlayGradient() =
    Spacer(
        modifier = Modifier
            .fillMaxHeight(0.25f)
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(Brush.verticalGradient(0f to Color(0x00000000), 1f to Color(0x9a000000)))
    )

@Composable
fun BoxScope.BottomEndDescriptions(content: @Composable ColumnScope.() -> Unit) =
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 40.dp).padding(end = 40.dp),
        content = content
    )

@Composable
fun ColumnScope.BottomEndDescription(text: String) =
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.45f),
        fontSize = 26.sp,
        fontFamily = FontFamily(Font(resource = "JetBrainsMono-Regular.ttf", weight = FontWeight.W400, style = FontStyle.Normal)),
        modifier = Modifier.padding(vertical = 5.dp)
    )

@Composable
fun ColumnScope.SelectSectionHeader(iconResource: String, title: String) =
    Box {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .size(550.dp, 50.dp)
                .offset(x = (-100).dp, y = 80.dp)
                .background(Color(0xff191919))
        ) {
            Text(
                text = title,
                fontFamily = FontFamily(Font(resource = "JetBrainsMono-Regular.ttf", weight = FontWeight.W400, style = FontStyle.Normal)),
                color = Color.White,
                fontSize = 28.sp,
                modifier = Modifier.padding(start = 275.dp)
            )
        }
        Image(
            painter = painterResource(iconResource),
            contentDescription = null,
            modifier = Modifier
                .size(125.dp)
                .drawBehind {
                    val color = Color(0xffff8800)
                    val verticalSize = Size(20.dp.value, 50.dp.value)
                    val horizontalSize = Size(50.dp.value, 20.dp.value)

                    val topLeft = Offset(-30.dp.value, -30.dp.value)
                    val bottomRight = Offset(size.width + 30.dp.value, size.height + 30.dp.value)

                    drawRect(color = color, topLeft = topLeft, size = horizontalSize)
                    drawRect(color = color, topLeft = topLeft, size = verticalSize)
                    drawRect(color = color, topLeft = bottomRight - verticalSize, size = verticalSize)
                    drawRect(color = color, topLeft = bottomRight - horizontalSize, size = horizontalSize)
                }
        )
    }

@Composable
fun SelectCandidateText(text: String, subtext: String, selected: Boolean = false) {
    Row {
        Spacer(modifier = Modifier.width(35.dp))
        Column {
            Text(
                text = subtext,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 20.sp,
                fontFamily = FontFamily(
                    Font(
                        resource = "JetBrainsMono-Regular.ttf",
                        weight = FontWeight.W400,
                        style = FontStyle.Normal
                    )
                ),
                modifier = Modifier.padding(top = 15.dp, bottom = 7.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = FontFamily(
                    Font(
                        resource = "JetBrainsMono-Regular.ttf",
                        weight = FontWeight.W400,
                        style = FontStyle.Normal
                    )
                ),
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
    }
}

operator fun Offset.minus(other: Size): Offset = this.minus(Offset(other.width, other.height))

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
fun BottomBarContainer(content: @Composable BoxScope.(DungeonsJsonState) -> Unit) =
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
                        arctic.stored = DungeonsJsonState(it.readDungeonsJson())
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
