package arctic.ui.composables

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import arctic.states.arctic
import arctic.ui.composables.fonts.JetbrainsMono
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.IngameImages


@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun TitleView() =
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

                BottomUnderlayGradient()

                ArrowDecor(modifier = Modifier.align(Alignment.CenterEnd).offset(x = (500).dp, y = (-650).dp))
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

                BottomEndDescriptions {
                    BottomEndDescription("Dungeons Editor, 1.0.0 by HoonKun")
                    BottomEndDescription("Compatible with Minecraft Dungeons 1.17.0.0")
                }

                Column(modifier = Modifier.align(Alignment.TopStart).fillMaxHeight().offset(x = 100.dp), verticalArrangement = Arrangement.Center) {
                    SelectSectionHeader("main_icon_history.svg", "Recent Files")
                    SelectCandidateText(text = "6762E9EB4A8B0A1ECF7989917F3E5366.dat", subtext = "~/minecraft/DungeonsData/SaveData")
                    SelectCandidateText(text = "AC9E38AD4D442E3EFD5BFC9177A2EBB9.dat", subtext = "~/minecraft/DungeonsData/SaveData")

                    Spacer(modifier = Modifier.height(125.dp))

                    SelectSectionHeader("main_icon_detected_files.svg", "Detected Files")
                    SelectCandidateText(text = "6762E9EB4A8B0A1ECF7989917F3E5366.dat", subtext = "~/minecraft/DungeonsData/SaveData")
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
fun ArrowDecor(modifier: Modifier = Modifier) =
    Text(
        text = "->",
        fontSize = 1000.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xff090500),
        textAlign = TextAlign.Start,
        fontFamily = JetbrainsMono,
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
        fontFamily = JetbrainsMono,
        modifier = Modifier.fillMaxWidth().align(Alignment.CenterEnd).scale(1.2f).offset(x = (-30).dp).then(modifier)
    )

@Composable
fun BoxScope.GoOverlay(text: String, color: Color, modifier: Modifier = Modifier) =
    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        fontFamily = JetbrainsMono,
        modifier = Modifier.align(Alignment.CenterEnd).then(modifier)
    )

@Composable
fun BoxScope.BottomUnderlayGradient() =
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
        fontFamily = JetbrainsMono,
        modifier = Modifier.padding(vertical = 5.dp)
    )

@Composable
fun ColumnScope.SelectSectionHeader(iconResource: String, title: String) {
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
                fontFamily = JetbrainsMono,
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
    Spacer(modifier = Modifier.height(75.dp))
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
                fontFamily = JetbrainsMono,
                modifier = Modifier.padding(top = 15.dp, bottom = 7.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = JetbrainsMono,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
    }
}

operator fun Offset.minus(other: Size): Offset = this.minus(Offset(other.width, other.height))
