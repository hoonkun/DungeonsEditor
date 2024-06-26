package kiwi.hoonkun.ui.composables

import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.Resources
import kiwi.hoonkun.ui.composables.base.BlurShadowImage
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.DungeonsJsonState
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.io.DungeonsJsonFile
import minecraft.dungeons.io.DungeonsSummary
import minecraft.dungeons.resources.DungeonsTextures

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JsonEntries(
    onJsonSelect: (newJson: DungeonsJsonState) -> Unit,
    focused: Boolean,
    modifier: Modifier = Modifier,
    preview: DungeonsJsonFile.Preview
) {
    val hasPreview = preview is DungeonsJsonFile.Preview.Valid

    val hideAlpha by minimizableAnimateFloatAsState(
        targetValue = if (hasPreview) 0f else 1f,
        animationSpec = minimizableSpec { spring() }
    )
    val hideIfPreviewNotPresents = remember { Modifier.graphicsLayer { alpha = 0.25f + (1 - hideAlpha) * 0.75f } }
    val hideIfPreviewPresents = remember { Modifier.graphicsLayer { alpha = 0.25f + hideAlpha * 0.75f } }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .then(modifier)
    ) {
        val recent = ArcticSettings.recentSummaries
        val detected = DungeonsJsonFile.Detector.results

        LazyColumn(contentPadding = PaddingValues(vertical = 36.dp)) {
            stickyHeader {
                SummaryHeader(
                    text = Localizations.UiText("file_selector_preview"),
                    modifier = hideIfPreviewNotPresents
                )
            }
            item {
                MinimizableAnimatedContent(
                    targetState = preview,
                    transitionSpec = minimizableContentTransform {
                        defaultFadeIn() togetherWith defaultFadeOut() using SizeTransform(clip = false)
                    }
                ) {
                    when (it) {
                        is DungeonsJsonFile.Preview.None -> {
                            SummaryNoEntries(
                                text = Localizations.UiText("file_selector_no_selection"),
                                modifier = hideIfPreviewNotPresents
                            )
                        }
                        is DungeonsJsonFile.Preview.Invalid -> {
                            SummaryNoEntries(
                                text = Localizations.UiText("file_selector_invalid"),
                                modifier = hideIfPreviewNotPresents
                            )
                        }
                        is DungeonsJsonFile.Preview.Valid -> {
                            JsonPreview(
                                summary = it.summary,
                                modifier = hideIfPreviewNotPresents,
                                onClick = { onJsonSelect(it.json) }
                            )
                        }
                    }
                }
            }
            item { SectionSpacing() }
            stickyHeader {
                SummaryHeader(
                    text = Localizations.UiText("recent_files"),
                    modifier = hideIfPreviewPresents
                )
            }
            if (recent.isEmpty()) {
                item {
                    SummaryNoEntries(
                        text = Localizations.UiText("no_recent_files"),
                        modifier = hideIfPreviewPresents
                    )
                }
            } else {
                items(recent, key = { it.first }) { (_, json, summary) ->
                    JsonPreview(
                        summary = summary,
                        onClick = { onJsonSelect(json) },
                        modifier = hideIfPreviewPresents.animateItemPlacement(
                            animationSpec = minimizableFiniteSpec { spring() }
                        )
                    )
                }
            }
            item { SectionSpacing() }
            stickyHeader {
                SummaryHeader(
                    text = Localizations.UiText("detected_files"),
                    modifier = hideIfPreviewPresents
                )
            }
            if (detected.isEmpty()) {
                item {
                    SummaryNoEntries(
                        text = Localizations.UiText("no_detected_files"),
                        modifier = hideIfPreviewPresents
                    )
                }
            } else {
                items(detected, key = { it.first }) { (_, json, summary) ->
                    JsonPreview(
                        summary = summary,
                        onClick = { onJsonSelect(json) },
                        modifier = hideIfPreviewPresents.animateItemPlacement(
                            animationSpec = minimizableFiniteSpec { spring() }
                        )
                    )
                }
            }
        }

        if (!focused) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeClick()
            )
        }
    }
}

@Composable
private fun SectionSpacing() =
    Spacer(modifier = Modifier.height(36.dp))

@Composable
private fun SummaryHeader(
    text: String,
    modifier: Modifier = Modifier
) =
    Text(
        text = text,
        modifier = modifier
            .width(225.dp)
            .padding(vertical = 16.dp)
            .background(Color(0xffff8800))
            .padding(start = 36.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
    )

@Composable
private fun SummaryNoEntries(
    text: String,
    modifier: Modifier = Modifier
) =
    Text(
        text = text,
        modifier = modifier
            .width(350.dp)
            .padding(vertical = 16.dp)
            .background(Color(0xff272727))
            .padding(start = 36.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
    )

@Composable
private fun JsonPreview(
    summary: DungeonsSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val source = rememberMutableInteractionSource()
    val hovered by source.collectIsHoveredAsState()

    val hoverOffset by minimizableAnimateFloatAsState(
        targetValue = if (hovered) 24f else 0f,
        animationSpec = minimizableSpec { spring() }
    )
    val hoverBackgroundAlpha by minimizableAnimateFloatAsState(
        targetValue = if (hovered) 0.15f else 0f,
        animationSpec = minimizableSpec { spring() }
    )

    Column(
        modifier = modifier
            .width(510.dp)
            .hoverable(source)
            .clickable(source, null) { onClick() }
            .offset { IntOffset(hoverOffset.dp.roundToPx(), 0) }
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = hoverBackgroundAlpha),
                    topLeft = Offset(-24.dp.toPx(), 0f),
                    size = Size(size.width + 24.dp.toPx(), size.height),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
            .padding(vertical = 24.dp, horizontal = 36.dp)
    ) {
        Row {
            CurrencyText(
                value = "${summary.level}",
                modifier = Modifier.weight(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CurrencyImage(DungeonsTextures["/Game/UI/Materials/Character/STATS_LV_frame.png"])
                    Text(text = "LV.", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            CurrencyText(
                iconTextureKey = "/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png",
                text = "${summary.power}",
                modifier = Modifier.weight(1f)
            )
            CurrencyText(
                iconTextureKey = "/Game/UI/Materials/Emeralds/emerald_indicator.png",
                text = "${summary.emerald}",
                iconScale = 22f / 28f,
                modifier = Modifier.weight(1f)
            )
            CurrencyText(
                iconTextureKey = "/Game/UI/Materials/Currency/GoldIndicator.png",
                text = "${summary.gold}",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            JsonPreviewEquipment(
                item = summary.melee,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            JsonPreviewEquipment(
                item = summary.armor,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            JsonPreviewEquipment(
                item = summary.ranged,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun JsonPreviewEquipment(
    item: Item?,
    modifier: Modifier = Modifier
) {
    if (item == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .aspectRatio(1f / 1f)
                .drawBehind { drawItemFrame("Common") }
        ) {
            Text(
                text = "-",
                fontSize = 30.sp,
                color = Color.White.copy(alpha = 0.45f),
                fontWeight = FontWeight.Light,
                modifier = Modifier.scale(scaleX = 2f, scaleY = 1f)
            )
        }
    } else {
        BlurShadowImage(
            bitmap = item.data.inventoryIcon,
            modifier = modifier
                .aspectRatio(1f / 1f),
            contentPadding = PaddingValues(all = 10.dp),
            onDrawBehind = { _, _ ->
                drawItemFrame(item.rarity, item.glided, item.enchanted, false)
            }
        )
    }
}

@Composable
private fun CurrencyText(
    iconTextureKey: String,
    text: String,
    modifier: Modifier = Modifier,
    iconScale: Float = 1f
) =
    CurrencyText(
        value = text,
        modifier = modifier
    ) {
        CurrencyImage(DungeonsTextures[iconTextureKey], iconScale)
    }

@Composable
private fun CurrencyText(
    value: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) =
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        icon()
        CurrencyTextContent(value)
    }

@Composable
private fun CurrencyTextContent(
    value: String
) =
    Text(
        text = value,
        fontFamily = Resources.Fonts.JetbrainsMono,
        fontSize = 20.sp,
        color = Color.White.copy(alpha = 0.85f),
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.wrapContentWidth().padding(start = 10.dp)
    )

@Composable
private fun CurrencyImage(
    image: ImageBitmap,
    iconScale: Float = 1f
) =
    Image(
        bitmap = image,
        contentDescription = null,
        modifier = Modifier.size(28.dp * iconScale)
    )

