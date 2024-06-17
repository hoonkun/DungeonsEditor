package kiwi.hoonkun.ui.composables

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.drawWithContent
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
import kiwi.hoonkun.ArcticSave
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.Resources
import kiwi.hoonkun.ui.composables.base.BlurShadowImage
import kiwi.hoonkun.ui.reusables.drawItemFrame
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.states.DungeonsJsonState
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import kiwi.hoonkun.utils.Retriever
import minecraft.dungeons.io.DungeonsSaveFile
import minecraft.dungeons.io.DungeonsSummary
import minecraft.dungeons.resources.DungeonsTextures

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JsonEntries(
    onJsonSelect: (newJson: DungeonsJsonState) -> Unit,
    modifier: Modifier = Modifier,
    hasPreview: Retriever<Boolean> = { false },
    preview: @Composable (
        none: @Composable () -> Unit,
        invalid: @Composable () -> Unit,
        valid: @Composable (DungeonsJsonState, DungeonsSummary) -> Unit
    ) -> Unit = { _, _, _ ->  }
) {
    val hideAlpha by animateFloatAsState(if (hasPreview()) 0f else 1f)
    val hideIfPreviewNotPresents = remember { Modifier.graphicsLayer { alpha = 0.25f + (1 - hideAlpha) * 0.75f } }
    val hideIfPreviewPresents = remember { Modifier.graphicsLayer { alpha = 0.25f + hideAlpha * 0.75f } }

    Row(modifier = Modifier.fillMaxHeight().then(modifier)) {
        val recent = ArcticSave.recentSummaries
        val detected = DungeonsSaveFile.Detector.results

        LazyColumn(
            contentPadding = PaddingValues(vertical = 36.dp)
        ) {
            stickyHeader {
                SummaryHeader(
                    text = Localizations.UiText("file_selector_preview"),
                    modifier = hideIfPreviewNotPresents
                )
            }
            item {
                preview(
                    { SummaryNoEntries(Localizations.UiText("file_selector_no_selection"), hideIfPreviewNotPresents) },
                    { SummaryNoEntries(Localizations.UiText("file_selector_invalid"), hideIfPreviewNotPresents) },
                    { state, summary -> JsonPreview(summary, hideIfPreviewNotPresents) { onJsonSelect(state) } }
                )
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
            }
            items(recent, key = { it.first }) { (_, json, summary) ->
                JsonPreview(
                    summary = summary,
                    onClick = { onJsonSelect(json) },
                    modifier = hideIfPreviewPresents
                )
            }
            item { SectionSpacing() }
            stickyHeader {
                SummaryHeader(Localizations.UiText("detected_files"), modifier = hideIfPreviewPresents)
            }
            if (detected.isEmpty()) {
                item { SummaryNoEntries(Localizations.UiText("no_detected_files"), modifier = hideIfPreviewPresents) }
            }
            items(detected, key = { it.first }) { (_, json, summary) ->
                JsonPreview(
                    summary = summary,
                    onClick = { onJsonSelect(json) },
                    modifier = hideIfPreviewPresents
                )
            }
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
) {
    Text(
        text = text,
        modifier = modifier
            .width(225.dp)
            .padding(vertical = 16.dp)
            .background(Color(0xffff8800))
            .padding(start = 36.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
    )
}

@Composable
private fun SummaryNoEntries(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .width(350.dp)
            .padding(vertical = 16.dp)
            .background(Color(0xff272727))
            .padding(start = 36.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
    )
}

@Composable
private fun JsonPreview(
    summary: DungeonsSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val source = rememberMutableInteractionSource()
    val hovered by source.collectIsHoveredAsState()

    val hoverOffset by animateFloatAsState(if (hovered) 24f else 0f)
    val hoverBackgroundAlpha by animateFloatAsState(if (hovered) 0.15f else 0f)

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
                icon = "/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png",
                value = "${summary.power}",
                modifier = Modifier.weight(1f)
            )
            CurrencyText(
                icon = "/Game/UI/Materials/Emeralds/emerald_indicator.png",
                value = "${summary.emerald}",
                small = true,
                modifier = Modifier.weight(1f)
            )
            CurrencyText(
                icon = "/Game/UI/Materials/Currency/GoldIndicator.png",
                value = "${summary.gold}",
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
                .drawWithContent {
                    drawItemFrame("Common")
                    drawContent()
                }
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
                .aspectRatio(1f / 1f)
                .drawWithContent {
                    drawItemFrame(item.rarity, item.glided, item.enchanted, false)
                    drawContent()
                }
                .padding(10.dp)
        )
    }
}

@Composable
private fun CurrencyText(
    icon: String,
    value: String,
    modifier: Modifier = Modifier,
    small: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        CurrencyImage(DungeonsTextures[icon], small)
        CurrencyTextContent(value)
    }
}

@Composable
private fun CurrencyText(
    value: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        icon()
        CurrencyTextContent(value)
    }
}

@Composable
private fun CurrencyTextContent(value: String) {
    Spacer(modifier = Modifier.width(10.dp))
    Text(
        text = value,
        fontFamily = Resources.Fonts.JetbrainsMono,
        fontSize = 20.sp,
        color = Color.White.copy(alpha = 0.85f),
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.wrapContentWidth()
    )
    Spacer(modifier = Modifier.width(30.dp))
}

@Composable
private fun CurrencyImage(image: ImageBitmap, small: Boolean = false) =
    Image(
        bitmap = image,
        contentDescription = null,
        modifier = Modifier.size(if (small) 22.dp else 28.dp)
    )

