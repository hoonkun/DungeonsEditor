package kiwi.hoonkun.ui.composables.overlays.tower

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.core.LocalWindowState
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.AutosizeText
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsTower
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.AnnotateTowerChallenge
import minecraft.dungeons.states.extensions.LocalizeTowerChallenge
import minecraft.dungeons.states.extensions.LocalizeTowerTile

@Composable
fun AnimatedVisibilityScope?.TowerTileChallengeOverlay(
    holder: MutableDungeons.TowerMissionState.Info.Config.Floor,

    initialTile: String,
    initialChallenges: List<String>,

    requestClose: () -> Unit
) {
    val density = LocalDensity.current
    val windowState = LocalWindowState.current

    val parentHeight = (windowState.size.height - DetailHeight) / 2f
    val childOffset = parentHeight / 2f - DetailHeight / 2f - 25.dp

    val tile = remember { mutableStateOf(initialTile) }
    val challenges = remember { mutableStateOf(initialChallenges.getOrNull(0)) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        TileDataCollection(
            state = tile,
            modifier = Modifier.minimizableAnimateEnterExit(
                scope = this@TowerTileChallengeOverlay,
                enter = minimizableEnterTransition { slideIn { IntOffset(-60.dp.value.toInt(), 0) } },
                exit = ExitTransition.None
            )
        )
        Spacer(modifier = Modifier.width(20.dp))
        Box(modifier = Modifier.requiredSize(DetailWidth, parentHeight)) {
            val animateModifier = Modifier
                .minimizableAnimateEnterExit(
                    scope = this@TowerTileChallengeOverlay,
                    enter = minimizableEnterTransition { slideIn { with(density) { IntOffset(0, -60.dp.roundToPx()) } } },
                    exit = ExitTransition.None
                )
                .animateContentSize(
                    animationSpec = minimizableFiniteSpec { spring(stiffness = Spring.StiffnessLow) }
                )

            TilePreview(
                state = tile,
                offset = childOffset,
                modifier = animateModifier
            )
            Row(
                modifier = Modifier
                    .offsetRelative(x = 0f, y = 1f)
                    .offset(y = (-50).dp)
                    .then(animateModifier)
                    .padding(vertical = 4.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                RetroButton(
                    text = Localizations["cancel"],
                    color = Color.White,
                    hoverInteraction = RetroButtonHoverInteraction.Overlay,
                    onClick = { requestClose() },
                    modifier = Modifier.size(125.dp, 55.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                RetroButton(
                    text = Localizations["ok"],
                    color = Color(0xff3f8e4f),
                    hoverInteraction = RetroButtonHoverInteraction.Outline,
                    onClick = {
                        val newTile = tile.value
                        val newChallenge = challenges.value

                        holder.tile = newTile

                        if (newChallenge == null)
                            holder.challenges.clear()
                        else {
                            if (holder.challenges.isNotEmpty())
                                holder.challenges[0] = newChallenge
                            else if (holder.challenges.isEmpty())
                                holder.challenges.add(newChallenge)
                        }

                        requestClose()
                    },
                    modifier = Modifier.size(125.dp, 55.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
        ChallengesDataCollection(
            state = challenges,
            modifier = Modifier.minimizableAnimateEnterExit(
                scope = this@TowerTileChallengeOverlay,
                enter = minimizableEnterTransition { slideIn { IntOffset(60.dp.value.toInt(), 0) } },
                exit = ExitTransition.None
            )
        )
    }
}

private val DetailHeight = 300.dp
private val DetailWidth = 500.dp

@Composable
private fun TilePreview(
    state: State<String>,
    offset: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(DetailWidth, DetailHeight)
            .offset(y = offset)
            .then(modifier)
            .background(Color(0xff080808))
            .clipToBounds()
            .consumeClick()
            .padding(vertical = 20.dp, horizontal = 30.dp)
    ) {
        Text(
            text = Localizations["todo_tile_preview"],
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TileDataCollection(
    state: MutableState<String>,
    modifier: Modifier = Modifier,
) {
    val tile by state

    val entries = DungeonsTower.tiles
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = entries.indexOf(tile).coerceAtLeast(0),
        initialFirstVisibleItemScrollOffset = -602.dp.value.toInt()
    )

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 30.dp),
        modifier = Modifier
            .requiredWidth(475.dp)
            .fillMaxHeight()
            .then(modifier)
            .background(Color(0xff080808))
    ) {
        items(entries) { data ->
            TileDataCollectionItem(data = data, state = state)
        }
    }
}

@Composable
private fun ChallengesDataCollection(
    state: MutableState<String?>,
    modifier: Modifier = Modifier,
) {
    val challenge by state

    val entries = DungeonsTower.challenges
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = entries.indexOf(challenge).coerceAtLeast(0),
        initialFirstVisibleItemScrollOffset = -602.dp.value.toInt()
    )

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 30.dp),
        modifier = Modifier
            .requiredWidth(475.dp)
            .fillMaxHeight()
            .then(modifier)
            .background(Color(0xff080808))
    ) {
        items(entries) { data ->
            ChallengesDataCollectionItem(data = data, state = state)
        }
    }
}

@Composable
private fun DataCollectionItem(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interaction)
            .clickable(interaction, null) { onClick() }
            .drawBehind {
                drawRect(
                    color = Color.White.copy(alpha = if (selected) 0.3f else if (hovered) 0.15f else 0f)
                )
            }
            .padding(vertical = 12.dp, horizontal = 24.dp)
    ) {
        content()
    }
}

@Composable
private fun TileDataCollectionItem(
    data: String,
    state: MutableState<String>
) {
    DataCollectionItem(selected = state.value == data, onClick = { state.value = data }) {
        Text(text = LocalizeTowerTile(data), fontSize = 26.sp)
        Text(text = data, fontSize = 18.sp, modifier = Modifier.alpha(0.5f))
    }
}

@Composable
private fun ChallengesDataCollectionItem(
    data: String,
    state: MutableState<String?>
) {
    DataCollectionItem(selected = state.value == data, onClick = { state.value = if (data == state.value) null else data }) {
        AutosizeText(text = AnnotateTowerChallenge(LocalizeTowerChallenge(data)), maxFontSize = 26.sp, maxLines = 1)
        AutosizeText(text = data, maxFontSize = 18.sp, modifier = Modifier.alpha(0.5f))
    }
}
