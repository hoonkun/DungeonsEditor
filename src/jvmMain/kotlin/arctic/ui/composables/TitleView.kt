package arctic.ui.composables

import LocalData
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import arctic.states.Arctic
import arctic.states.EditorState
import arctic.ui.composables.atomic.BlurEffectedImage
import arctic.ui.composables.atomic.RetroButton
import arctic.ui.composables.atomic.densityDp
import arctic.ui.composables.atomic.drawItemFrame
import arctic.ui.composables.fonts.JetbrainsMono
import arctic.ui.composables.overlays.extended.defaultFadeIn
import arctic.ui.composables.overlays.extended.defaultFadeOut
import arctic.ui.composables.overlays.extended.defaultSlideIn
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import arctic.ui.utils.rememberMutableInteractionSource
import dungeons.DungeonsJsonFile
import dungeons.DungeonsSummary
import dungeons.IngameImages
import dungeons.Localizations
import dungeons.states.DungeonsJsonState
import dungeons.states.Item
import extensions.lengthEllipsisMiddle
import utils.separatePathAndName


private typealias DungeonsFileInfo = Triple<String, DungeonsJsonState, DungeonsSummary>
private fun findDungeonsFileInfo(key: String?): DungeonsFileInfo? =
    LocalData.recentSummaries.find { it.first == key } ?: DungeonsJsonFile.detected.find { it.first == key }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TitleView(modifier: Modifier = Modifier) {
    var selectedPath by remember { mutableStateOf<String?>(null) }
    val selectedState by derivedStateOf { findDungeonsFileInfo(selectedPath)?.second }
    val selectedSummary by derivedStateOf { findDungeonsFileInfo(selectedPath)?.third }

    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()

    val goAlpha by animateFloatAsState(if (pressed) 0.55f else if (hovered || focused) 0.4f else 0.3f)
    val summaryOffset by animateFloatAsState(if (hovered || focused) 50f else 0f)
    val summaryContainerOffset by animateFloatAsState(if (hovered || focused) -20f else 0f)

    AnimatedContent(
        targetState = Arctic.editorState == null,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = Modifier.fillMaxSize().then(modifier)
    ) { isInTitle ->
        if (isInTitle) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AnimatedVisibility(
                    visible = Arctic.pakState == Arctic.PakState.Initialized,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Image(
                        bitmap = IngameImages.get { "/Game/UI/Materials/LoadingScreens/Loading_Ancient_Hunt.png" },
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { renderEffect = BlurEffect(50.dp.value, 50.dp.value) }
                            .drawWithContent {
                                drawContent()
                                drawRect(Color.Black.copy(alpha = 0.6f))
                            }
                    )
                }

                BottomUnderlayGradient()

                ArrowDecor(modifier = Modifier.align(Alignment.CenterEnd).offset(x = (500).dp, y = (-650).dp))
                ArrowDecor(modifier = Modifier.align(Alignment.CenterStart).offset(x = (-300).dp, y = 425.dp))

                Go(color = Color(0xffff8800), modifier = Modifier.graphicsLayer { renderEffect = BlurEffect(125.dp.value, 125.dp.value) }.graphicsLayer { alpha = goAlpha })
                Go(color = Color(0xff090500))

                Clickable(
                    modifier = Modifier
                        .hoverable(interaction)
                        .clickable(interaction, null) {
                            val json = selectedState
                            val path = selectedPath
                            if (path == null || json == null) {
                                Arctic.overlayState.fileLoadSrcSelector = true
                            } else {
                                Arctic.editorState = EditorState(json)
                                LocalData.updateRecentFiles(path)
                            }
                        }
                )

                GoOverlay(
                    text = "Getting Started",
                    color = Color(0xff090500),
                    modifier = Modifier
                        .offset(x = (-295).dp, y = (-260).dp)
                        .height(52.dp)
                        .background(Color(0xffff8800))
                        .padding(start = 15.dp, top = 5.dp, bottom = 5.dp, end = 415.dp)
                )
                RetroButton(
                    text = Localizations.Names[LocalData.locale] ?: "",
                    color = Color(0xffa25400),
                    fontFamily = JetbrainsMono,
                    hoverInteraction = "outline",
                    buttonSize = (if (LocalData.locale == "en") 225.dp else 145.dp) to 52.dp,
                    radius = 4f,
                    stroke = 2f,
                    maxFontSize = 18.sp,
                    useAutoSizeText = true,
                    modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-295).dp, y = (-260).dp)
                ) {
                    LocalData.locale = if (LocalData.locale == "en") "ko-KR" else "en"
                    LocalData.save()
                }

                Divider(alignment = Alignment.Top)
                Divider(alignment = Alignment.Bottom)

                BottomEndDescriptions {
                    BottomEndDescription("Dungeons Editor, 1.0.0 by HoonKun")
                    BottomEndDescription("Compatible with Minecraft Dungeons 1.17.0.0")
                }

                Summary(
                    selectedSummary,
                    rootModifier = Modifier.offset { IntOffset(x = summaryOffset.dp.value.toInt(), y = 0) },
                    contentModifier = Modifier.offset { IntOffset(x = summaryContainerOffset.dp.value.toInt(), y = 0) }
                ) { summary ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(x = (-80).dp)
                    ) {
                        CurrenciesSummary(summary)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.offset(x = (-15).dp)
                    ) {
                        EquipmentSummary(summary)
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.TopStart).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    SelectSectionHeader("main_icon_history.svg", Localizations.UiText("recent_files"))
                    for (recent in LocalData.recentSummaries) {
                        val (path) = recent
                        val (text, subtext) = separatePathAndName(path)
                        SelectCandidateText(
                            text = text,
                            subtext = subtext,
                            selected = selectedPath == path,
                            onClick = { selectedPath = if (selectedPath == path) null else path }
                        )
                    }
                    if (LocalData.recentFiles.isEmpty()) {
                        NoCandidateText(text = Localizations.UiText("no_recent_files"))
                    }

                    Spacer(modifier = Modifier.height(125.dp))

                    SelectSectionHeader("main_icon_detected_files.svg", Localizations.UiText("detected_files"))
                    for (detected in DungeonsJsonFile.detected) {
                        val (path) = detected
                        val (text, subtext) = separatePathAndName(path)
                        SelectCandidateText(
                            text = text,
                            subtext = subtext,
                            selected = selectedPath == path,
                            onClick = { selectedPath = if (selectedPath == path) null else path }
                        )
                    }
                    if (DungeonsJsonFile.detected.isEmpty()) {
                        NoCandidateText(text = Localizations.UiText("no_detected_files"))
                    }
                }

            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun BoxScope.Clickable(modifier: Modifier) =
    Spacer(
        modifier = Modifier
            .size(825.dp, 500.dp)
            .offset(y = 200.dp)
            .align(Alignment.CenterEnd)
            .then(modifier)
    )

@Composable
private fun BoxScope.Summary(
    targetState: DungeonsSummary?,
    rootModifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    content: @Composable ColumnScope.(DungeonsSummary) -> Unit
) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.requiredSize(1200.dp, 800.dp).align(Alignment.CenterEnd).offset(x = 225.dp, y = 400.dp).then(rootModifier)
    ) {
        Text(
            text = "->",
            fontFamily = JetbrainsMono,
            fontSize = 825.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xff191919),
            modifier = Modifier.offset(y = (-320).dp)
        )
        AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                val enter = defaultFadeIn() + defaultSlideIn { IntOffset(-50.dp.value.toInt(), 0) }
                val exit = defaultFadeOut()
                enter togetherWith exit using SizeTransform(false)
            },
            modifier = Modifier.fillMaxSize()
        ) { summary ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().offset(x = (-60).dp, y = (-275).dp).scale(1.35f).then(contentModifier)
            ) {
                if (summary != null) content(summary)
            }
        }
    }

@Composable
private fun CurrenciesSummary(summary: DungeonsSummary) {
    CurrencyText(value = "${summary.level}") {
        Box(contentAlignment = Alignment.Center) {
            CurrencyImage(IngameImages.get { "/Game/UI/Materials/Character/STATS_LV_frame.png" })
            Text(text = "LV.", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
    CurrencyText(
        icon = "/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png",
        value = "${summary.power}"
    )
    CurrencyText(
        icon = "/Game/UI/Materials/Emeralds/emerald_indicator.png",
        value = "${summary.emerald}",
        small = true
    )
    CurrencyText(
        icon = "/Game/UI/Materials/Currency/GoldIndicator.png",
        value = "${summary.gold}"
    )
}

@Composable
private fun CurrencyText(icon: String, value: String, small: Boolean = false) {
    CurrencyImage(IngameImages.get { icon }, small)
    CurrencyTextContent(value)
}

@Composable
private fun CurrencyText(value: String, icon: @Composable () -> Unit) {
    icon()
    CurrencyTextContent(value)
}

@Composable
private fun CurrencyTextContent(value: String) {
    Spacer(modifier = Modifier.width(10.dp))
    Text(text = value, fontFamily = JetbrainsMono, fontSize = 20.sp, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.wrapContentWidth())
    Spacer(modifier = Modifier.width(30.dp))
}

@Composable
private fun CurrencyImage(image: ImageBitmap, small: Boolean = false) =
    Image(image, null, modifier = Modifier.size(if (small) 22.dp else 28.dp))

@Composable
private fun EquipmentSummary(summary: DungeonsSummary) {
    EquipmentSummaryEach(summary.melee)
    Spacer(modifier = Modifier.width(30.dp))
    EquipmentSummaryEach(summary.armor)
    Spacer(modifier = Modifier.width(30.dp))
    EquipmentSummaryEach(summary.ranged)
}

@Composable
private fun EquipmentSummaryEach(item: Item?) {
    if (item != null) {
        BlurEffectedImage(
            bitmap = item.data.inventoryIcon,
            containerContentAlignment = Alignment.Center,
            containerModifier = Modifier
                .size(120.dp)
                .drawWithContent {
                    drawItemFrame(
                        rarity = item.rarity,
                        glided = item.netheriteEnchant != null,
                        enchanted = item.totalEnchantmentInvestedPoints > 0,
                        artifact = item.data.variant == "Artifact"
                    )
                }
                .padding(10.dp)
                .scale(2f),
            imageModifier = Modifier.scale(0.5f)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .drawWithContent { drawItemFrame(rarity = "Common", glided = false, enchanted = false, artifact = false) }
        ) {
            Text("/", fontSize = 30.sp, color = Color.White.copy(alpha = 0.45f), fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
private fun BoxScope.Divider(alignment: Alignment.Vertical) =
    Spacer(
        modifier = Modifier
            .size(50.dp, 1.dp)
            .align(if (alignment == Alignment.Top) Alignment.TopCenter else Alignment.BottomCenter)
            .offset(y = if (alignment == Alignment.Top) 125.dp else (-125).dp)
            .background(Color.White.copy(alpha = 0.25f))
    )

@Composable
private fun ArrowDecor(modifier: Modifier = Modifier) =
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
private fun BoxScope.Go(color: Color, modifier: Modifier = Modifier) =
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
private fun BoxScope.GoOverlay(text: String, color: Color, modifier: Modifier = Modifier) =
    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        fontFamily = JetbrainsMono,
        modifier = Modifier.align(Alignment.CenterEnd).then(modifier)
    )

@Composable
private fun BoxScope.BottomUnderlayGradient() =
    Spacer(
        modifier = Modifier
            .fillMaxHeight(0.25f)
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(Brush.verticalGradient(0f to Color(0x00000000), 1f to Color(0x9a000000)))
    )

@Composable
private fun BoxScope.BottomEndDescriptions(content: @Composable ColumnScope.() -> Unit) =
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 40.dp).padding(end = 40.dp),
        content = content
    )

@Composable
private fun BottomEndDescription(text: String) =
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.45f),
        fontSize = 26.sp,
        fontFamily = JetbrainsMono,
        modifier = Modifier.padding(vertical = 5.dp)
    )

@Composable
private fun SelectSectionHeader(iconResource: String, title: String) {
    Box(modifier = Modifier.padding(start = 100.dp)) {
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
                    val verticalSize = Size(densityDp(20), densityDp(50))
                    val horizontalSize = Size(densityDp(50), densityDp(20))

                    val topLeft = Offset(densityDp(-30), densityDp(-30))
                    val bottomRight = Offset(size.width + densityDp(30), size.height + densityDp(30))

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
private fun SelectCandidateText(text: String, subtext: String, selected: Boolean, onClick: () -> Unit) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val focused by interaction.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onClick)
            .background(Color.White.copy(alpha = if (selected) 0.1f else if (hovered || focused) 0.075f else 0.0f))
            .padding(start = 100.dp)
    ) {
        Column {
            Text(
                text = subtext.lengthEllipsisMiddle(40),
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
        Spacer(modifier = Modifier.width(30.dp))
    }
}

@Composable
private fun NoCandidateText(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.4f),
        fontSize = 22.sp,
        fontFamily = JetbrainsMono,
        modifier = Modifier.padding(bottom = 15.dp, start = 100.dp)
    )
}

operator fun Offset.minus(other: Size): Offset = this.minus(Offset(other.width, other.height))
