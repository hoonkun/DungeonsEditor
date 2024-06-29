package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import kiwi.hoonkun.core.LocalWindowState
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.*
import kiwi.hoonkun.ui.composables.editor.collections.ItemSlot
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsSkeletons
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.skeleton
import minecraft.dungeons.states.extensions.withEnchantments
import kotlin.math.roundToInt


@Stable
private class EnchantmentDataCollectionState(
    holder: MutableDungeons.Item,
    initialSelected: MutableDungeons.Enchantment
) {
    val datasets = DungeonsSkeletons.Enchantment[Unit].filter { it.applyFor.contains(holder.skeleton.variant) }
    val initialIndex = datasets.indexOf(initialSelected.skeleton)

    val netherite = holder.netheriteEnchant?.copy() ?: MutableDungeons.Enchantment(isNetheriteEnchant = true)
    val enchantments = holder.enchantments.map { it.copy() }.toMutableStateList()

    var selected by mutableStateOf(enchantments.getOrNull(holder.enchantments.indexOf(initialSelected)) ?: netherite)
}

@Composable
private fun rememberEnchantmentDataCollectionState(holder: MutableDungeons.Item, original: MutableDungeons.Enchantment) =
    remember(original) { EnchantmentDataCollectionState(holder, original) }

@Composable
fun AnimatedVisibilityScope?.EnchantmentOverlay(
    holder: MutableDungeons.Item,
    initialSelected: MutableDungeons.Enchantment,
    requestClose: () -> Unit
) {
    val density = LocalDensity.current
    val windowState = LocalWindowState.current

    val parentHeight = (windowState.size.height - DetailHeight) / 2f
    val childOffset = parentHeight / 2f - DetailHeight / 2f - 25.dp

    val state = rememberEnchantmentDataCollectionState(holder, initialSelected)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        EnchantmentDataCollection(
            state = state,
            modifier = Modifier
                .minimizableAnimateEnterExit(
                    scope = this@EnchantmentOverlay,
                    enter = minimizableEnterTransition { slideIn { with(density) { IntOffset(-60.dp.roundToPx(), 0) } } },
                    exit = ExitTransition.None
                )
        )
        Spacer(modifier = Modifier.width(40.dp))
        Box(modifier = Modifier.requiredSize(675.dp, parentHeight)) {
            HolderPreview(
                state = state,
                holder = holder,
                offset = childOffset,
                modifier = Modifier
                    .minimizableAnimateEnterExit(
                        scope = this@EnchantmentOverlay,
                        enter = minimizableEnterTransition { slideIn { with(density) { IntOffset(60.dp.roundToPx(), 0) } } },
                        exit = ExitTransition.None
                    )
            )

            MinimizableAnimatedContent(
                targetState = state.selected,
                transitionSpec = minimizableContentTransform spec@ {
                    val initialIndex = state.enchantments.indexOf(initialState)
                    val targetIndex = state.enchantments.indexOf(targetState)

                    val offset = with(density) {
                        if (initialIndex < targetIndex) 50.dp.roundToPx()
                        else (-50).dp.roundToPx()
                    }

                    val enter = defaultFadeIn() + slideIn { IntOffset(offset, 0) }
                    val exit = defaultFadeOut() + slideOut { IntOffset(-offset, 0) }

                    enter togetherWith exit using SizeTransform(clip = false)
                },
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .minimizableAnimateEnterExit(
                        scope = this@EnchantmentOverlay,
                        enter = minimizableEnterTransition { slideIn { with(density) { IntOffset(0, 60.dp.roundToPx()) } } },
                        exit = ExitTransition.None
                    )
            ) { preview ->
                EnchantmentDetail(
                    enchantment = preview,
                    onLevelChange = { withEnchantments { preview.applyInvestedPoints(holder.glided, it) } }
                )
                Row(modifier = Modifier.offsetRelative(x = 0f, y = 1f).offset(y = -childOffset)) {
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
                            holder.enchantments.clear()
                            holder.enchantments.addAll(state.enchantments)

                            holder.netheriteEnchant = state.netherite

                            requestClose()
                        },
                        modifier = Modifier.size(125.dp, 55.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HolderPreview(
    state: EnchantmentDataCollectionState,
    holder: MutableDungeons.Item,
    offset: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .offset(y = offset)
            .offsetRelative(x = 0f, y = -1f)
            .then(modifier)
            .background(Color(0xff080808))
            .clipToBounds()
            .drawBehind {
                drawImage(
                    image = holder.skeleton.largeIcon,
                    dstOffset = Offset((-20f).dp.toPx(), -30f.dp.toPx()).round(),
                    dstSize = Size(size.width * 0.5f, size.width * 0.5f).round(),
                    alpha = 0.25f
                )
            }
            .consumeClick()
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemSlot(
                item = holder,
                fontSize = 12.sp,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(1f / 1f)
                    .padding(all = 12.dp)
            )
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 8.dp)
                ) {
                    ItemRarityButton(data = holder.skeleton, rarity = holder.rarity, readonly = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    ItemNetheriteEnchantButton(
                        modifier = Modifier
                            .drawBehind {
                                if (state.selected != state.netherite) return@drawBehind
                                drawInteractionBorder(hovered = false, selected = true)
                            },
                        enchantment = state.netherite
                    ) {
                        state.selected = state.netherite
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ItemModifiedButton(holder = holder, readonly = true, hideUnits = true)
                }
                Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                    AutosizeText(
                        text = holder.skeleton.name,
                        maxFontSize = 40.sp,
                        style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .weight(1f)
                            .alignByBaseline()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .alignByBaseline()
                            .padding(start = 16.dp)
                            .requiredWidth(100.dp)
                    ) {
                        PowerIcon(Modifier.size(24.dp))
                        Text(
                            text = "${holder.power.roundToInt()}",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.drawBehind { drawEnchantmentRune(topOffset = (-150).dp) }) {
            val slots = remember { state.enchantments.chunked(3).map { MutableEnchantments(it) } }
            slots.forEach { slot ->
                EnchantmentSlot(
                    enchantments = slot,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f / 1f)
                ) { each ->
                    val enabled by remember {
                        derivedStateOf { slot.all.find { it.level > 0 }?.let { each === it } != false }
                    }

                    val scale by minimizableAnimateFloatAsState(
                        targetValue = if (each.level > 0) 1.25f else 1f,
                        animationSpec = minimizableSpec { spring() }
                    )
                    val grayscaleAmount by minimizableAnimateFloatAsState(
                        targetValue = if (enabled) 1f else 0f,
                        animationSpec = minimizableSpec { spring() }
                    )

                    MinimizableAnimatedContent(
                        targetState = each.skeleton,
                        transitionSpec = minimizableContentTransform {
                            val enter =
                                if (targetState.id == "Unset") defaultFadeIn()
                                else defaultFadeIn() + scaleIn(initialScale = 1.5f)
                            val exit = defaultFadeOut()
                            enter togetherWith exit using SizeTransform(clip = false)
                        },
                        modifier = Modifier
                            .drawWithCache {
                                onDrawBehind {
                                    if (state.selected !== each) return@onDrawBehind

                                    val dstSize = (size * 0.8f * scale)
                                    val dstOffset = center - (dstSize / 2f).run { Offset(width, height) }

                                    drawPath(
                                        path = EnchantmentOutlinePath(dstOffset.round(), dstSize.round()),
                                        color = Color.White,
                                        style = Stroke(width = 6.dp.toPx())
                                    )
                                }
                            }
                    ) {
                        EnchantmentImage(
                            data = it,
                            enabled = enabled,
                            onClick = { state.selected = each },
                            modifier = Modifier.fillMaxSize()
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .grayscale { grayscaleAmount }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnchantmentDataCollection(
    state: EnchantmentDataCollectionState,
    modifier: Modifier = Modifier
) {
    val windowState = LocalWindowState.current
    val density = LocalDensity.current

    val autoScrollOffset = remember(density, windowState.size) {
        with(density) { -(windowState.size.height / 2f).roundToPx() + 160.dp.roundToPx() }
    }

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = state.initialIndex,
        initialFirstVisibleItemScrollOffset = autoScrollOffset
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        state = gridState,
        contentPadding = PaddingValues(vertical = 60.dp, horizontal = 10.dp),
        modifier = Modifier
            .requiredWidth(700.dp)
            .fillMaxHeight()
            .then(modifier)
            .background(Color(0xff080808))
    ) {
        items(state.datasets, key = { it.id }) { data ->
            val isUniqueInHolder = remember(data, state.selected, state.selected.id) {
                state.enchantments.all { it.skeleton.id != data.id }
            }

            EnchantmentDataCollectionItem(
                data = data,
                enabled = state.selected.id == data.id || data.stackable || isUniqueInHolder,
                selected = state.selected.id == data.id,
                onItemSelect = { newData ->
                    val newId = if (newData.id == state.selected.id) "Unset" else newData.id
                    state.selected.id = newId
                    state.selected.level =
                        if (newId == "Unset")
                            0
                        else if (state.selected.isNetheriteEnchant)
                            state.selected.level.coerceAtLeast(1)
                        else
                            state.selected.level
                }
            )
        }
    }
}


@Composable
private fun EnchantmentDataCollectionItem(
    data: DungeonsSkeletons.Enchantment,
    enabled: Boolean,
    selected: Boolean,
    onItemSelect: (DungeonsSkeletons.Enchantment) -> Unit
) {
    val deleteIcon = remember { useResource("ic_close.png") { loadImageBitmap(it) } }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        EnchantmentImage(
            data = data,
            selected = selected,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / 1f)
                .graphicsLayer { alpha = if (enabled) 1f else 0.25f },
            contentPaint = { if (selected) SelectedPaint else it },
            onDrawFront = onDrawFront@ {
                if (!selected) return@onDrawFront

                val dstSize = size * 0.35f
                val dstOffset = center - (dstSize / 2f).run { Offset(width, height) }
                drawImage(
                    image = deleteIcon,
                    dstSize = dstSize.round(),
                    dstOffset = dstOffset.round(),
                )
            },
            onClick = { onItemSelect(data) }
        )
        AutosizeText(
            text = data.name,
            style = LocalTextStyle.current.copy(
                color = Color.White.copy(alpha = if (enabled || selected) 1f else 0.5f),
                fontWeight = if (enabled || selected) FontWeight.Bold else FontWeight.Normal,
            ),
            maxFontSize = 20.sp
        )
        Text(
            text = Localizations["effect_delete"],
            style = LocalTextStyle.current.copy(fontSize = 14.sp, textAlign = TextAlign.Center),
            modifier = Modifier.graphicsLayer { alpha = if (selected) 1f else 0f }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

private val SelectedPaint = Paint().apply {
    colorFilter = ColorFilter.lighting(Color(red = 0.35f, green = 0.35f, blue = 0.35f), Color.Black)
}

private val DetailHeight get() = 300.dp

@Composable
private fun EnchantmentDetail(
    enchantment: MutableDungeons.Enchantment,
    onLevelChange: (Int) -> Unit
) {
    val data = remember(enchantment.id) { DungeonsSkeletons.Enchantment[Unit].first { it.id == enchantment.id } }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .requiredSize(675.dp, DetailHeight)
            .background(Color(0xff080808))
            .consumeClick()
    ) {
        MinimizableAnimatedContent(
            targetState = data,
            transitionSpec = minimizableContentTransform spec@ {
                val initialIndex = DungeonsSkeletons.Enchantment[Unit].indexOf(initialState)
                val targetIndex = DungeonsSkeletons.Enchantment[Unit].indexOf(targetState)

                val offset = with(density) {
                    if (initialIndex < targetIndex) 50.dp.roundToPx()
                    else (-50).dp.roundToPx()
                }

                val enter = defaultFadeIn() + slideIn { IntOffset(0, offset) }
                val exit = defaultFadeOut() + slideOut { IntOffset(0, -offset) }

                enter togetherWith exit
            },
            contentAlignment = Alignment.Center
        ) { capturedData ->
            if (capturedData.id == "Unset") {
                UnsetEnchantmentPreview()
            } else {
                ValidEnchantmentPreview(
                    data = capturedData,
                    parent = enchantment,
                    onLevelChange = onLevelChange
                )
            }
        }
    }
}

@Composable
private fun UnsetEnchantmentPreview() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text(
            text = Localizations["enchantment_empty_slot"],
            modifier = Modifier.alpha(0.75f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ValidEnchantmentPreview(
    data: DungeonsSkeletons.Enchantment,
    parent: MutableDungeons.Enchantment,
    onLevelChange: (Int) -> Unit
) {
    Row {
        Box(modifier = Modifier.fillMaxHeight().aspectRatio(1f / 1f)) {
            EnchantmentImage(
                data = data,
                enabled = false,
                modifier = Modifier.fillMaxSize()
            )
            EnchantmentLevel(level = parent.level)
        }
        Column(modifier = Modifier.padding(end = 36.dp).padding(vertical = 36.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                EnchantmentNameText(data.name)
                if (data.powerful)
                    PowerfulEnchantmentIndicator()
            }

            IfNotNull(data.description) {
                Text(text = it, fontSize = 16.sp, color = Color.White.copy(0.75f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            IfNotNull(data.effect) {
                Text(text = it, fontSize = 20.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))
            EnchantmentLevelSelector(
                isNetheriteEnchant = parent.isNetheriteEnchant,
                currentLevel = parent.level,
                onLevelChange = onLevelChange
            )
        }
    }
}

@Composable
private fun EnchantmentNameText(text: String) =
    Text(
        text = text,
        style = LocalTextStyle.current.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold),
        overflow = TextOverflow.Ellipsis
    )

@Composable
private fun EnchantmentLevelSelector(
    isNetheriteEnchant: Boolean,
    currentLevel: Int,
    onLevelChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!isNetheriteEnchant) {
            RetroButton(
                text = Localizations["enchantment_deactivate"],
                color = if (currentLevel == 0) Color(0xfff54242) else Color.Transparent,
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = { onLevelChange(0) },
                modifier = Modifier.weight(1.5f)
            )
        }

        for (indicatingLevel in 0..3) {
            RetroButton(
                color = if (currentLevel == 0) Color(0xfff54242) else Color.Transparent,
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = { onLevelChange(0) },
                modifier = Modifier.padding(start = 4.dp).weight(1.5f)
            ) {
                Image(
                    bitmap = LevelImage(indicatingLevel),
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun PowerfulEnchantmentIndicator() =
    Text(
        text = DungeonsLocalizations["/enchantment_rarity_powerful"]!!,
        style = TextStyle(color = Color(0xffe5247e), fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(start = 10.dp, bottom = 3.dp)
    )

@Stable
private fun LevelImage(level: Int) =
    if (level == 0) DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment/behind_enchantments_whole_switch.png"]
    else DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${level}_normal_text.png"]
