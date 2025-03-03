package kiwi.hoonkun.ui.composables.overlays.tower

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.composables.base.RetroIndicator
import kiwi.hoonkun.ui.composables.editor.collections.ItemHoverBorderModifier
import kiwi.hoonkun.ui.composables.editor.collections.ItemSlot
import kiwi.hoonkun.ui.composables.editor.details.ItemDetailContent
import kiwi.hoonkun.ui.reusables.minimizableAnimateEnterExit
import kiwi.hoonkun.ui.reusables.offsetRelative
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsSkeletons
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.skeleton
import minecraft.dungeons.values.DungeonsItem
import minecraft.dungeons.values.asSerializedPower
import minecraft.dungeons.values.toSerialized

@Composable
fun AnimatedVisibilityScope?.TowerItemModificationOverlay(
    oldItem: MutableDungeons.Item?,
    onUpdate: (MutableDungeons.Item) -> Unit,
    requestClose: () -> Unit
) {

    var item by remember(oldItem) { mutableStateOf(oldItem?.copy()?.also { it.markedNew = false }) }

    val density = LocalDensity.current

    val transitionOffset = with(density) { -30.dp.roundToPx() }

    val filterBy = item?.skeleton?.variant ?: DungeonsItem.Variant.Artifact
    val filteredEntries = DungeonsSkeletons.Item[Unit]
        .filter { it.variant == filterBy }
        .sortedBy { it.name }
        .sortedBy { !it.unique }
    val initialFirstVisibleItemIndex = filteredEntries
        .indexOfFirst { it.type == item?.type }
        .coerceAtLeast(0)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.horizontalGradient(
                        0f to Color(0xff080808).copy(alpha = 0.75f),
                        1f to Color(0xff080808).copy(alpha = 0f),
                        startX = size.width * 0.45f,
                        endX = size.width
                    )
                )
            }
            .minimizableAnimateEnterExit(
                this@TowerItemModificationOverlay,
                enter = fadeIn() + slideInHorizontally { transitionOffset },
                exit = fadeOut() + slideOutHorizontally { transitionOffset },
            )
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
            state = rememberLazyListState(
                initialFirstVisibleItemIndex = initialFirstVisibleItemIndex,
                initialFirstVisibleItemScrollOffset = with(density) { -602.dp.roundToPx() }
            ),
            modifier = Modifier
                .fillMaxHeight()
                .background(color = Color.Black)
                .padding(horizontal = 48.dp)
        ) {

            items(filteredEntries) {
                val interaction = rememberMutableInteractionSource()
                val hovered by interaction.collectIsHoveredAsState()

                val onItemClick = {
                    val capturedItem = item
                    if (capturedItem != null) {
                        capturedItem.type = it.type
                        if (capturedItem.skeleton.variant == DungeonsItem.Variant.Armor) {
                            capturedItem.armorProperties.clear()
                            capturedItem.armorProperties.addAll(
                                it.builtInProperties
                                    .map { property -> MutableDungeons.ArmorProperty(id = property.id) }
                            )
                        }
                    } else {
                        item = MutableDungeons.Item(
                            type = it.type,
                            power = 0.0.asSerializedPower(),
                            rarity = DungeonsItem.Rarity.Common
                        )
                    }
                }

                ItemSlot(
                    MutableDungeons.Item(
                        type = it.type,
                        rarity = if (it.unique) DungeonsItem.Rarity.Unique else DungeonsItem.Rarity.Common,
                        power = item?.power?.toSerialized() ?: 0.0.asSerializedPower(),
                    ),
                    hideDecorations = true,
                    modifier = Modifier
                        .width(125.dp)
                        .aspectRatio(1f)
                        .clickable(interactionSource = interaction, indication = null, onClick = onItemClick)
                        .hoverable(interaction)
                        .then(ItemHoverBorderModifier(item?.type == it.type, hovered))
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.width(950.dp)
                .padding(horizontal = 32.dp)
                .drawWithCache {
                    val path = RetroIndicator()
                    onDrawBehind { drawPath(path, color = Color.Black) }
                }
                .padding(start = 64.dp, end = 64.dp, top = 16.dp)
        ) {
            AnimatedContent(
                targetState = item,
                transitionSpec = {
                    val enter = fadeIn() + slideInVertically { with(density) { -30.dp.roundToPx() } }
                    val exit = fadeOut() + slideOutVertically { with(density) { -30.dp.roundToPx() } }

                    enter togetherWith exit using SizeTransform(false)
                },
            ) {
                Box(modifier = Modifier.clipToBounds().padding(bottom = 12.dp)) {
                    if (it != null)
                        ItemDetailContent(it)
                    else
                        Text(
                            text = "왼쪽 목록에서 추가할 아이템을 선택해주세요.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .alpha(0.7f)
                                .padding(vertical = 64.dp)
                        )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 64.dp, y = 24.dp)
                    .offsetRelative(y = 1.0f)
            ) {
                RetroButton(
                    text = Localizations["cancel"],
                    color = Color(0xffff6e25),
                    hoverInteraction = RetroButtonHoverInteraction.Outline,
                    onClick = requestClose
                )
                Spacer(modifier = Modifier.width(24.dp))
                RetroButton(text = Localizations["ok"],
                    color = Color(0xff3f8e4f),
                    hoverInteraction = RetroButtonHoverInteraction.Outline,
                    enabled = item != null,
                    onClick = click@ {
                        onUpdate(item ?: return@click)
                        requestClose()
                    }
                )
            }
        }
    }

}
