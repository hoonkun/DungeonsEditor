package kiwi.hoonkun.ui.composables.overlays

import LocalWindowState
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.*
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
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.*
import kiwi.hoonkun.ui.composables.editor.collections.ItemSlot
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.Enchantment
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsDatabase
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.resources.EnchantmentData
import minecraft.dungeons.values.DungeonsPower
import kotlin.math.roundToInt


@Stable
private class EnchantmentDataCollectionState(
    initialSelected: Enchantment
) {
    val holder = initialSelected.holder

    val datasets = DungeonsDatabase.enchantments.filter { it.applyFor.contains(initialSelected.holder.data.variant) }
    val initialIndex = datasets.indexOf(initialSelected.data)

    val netherite = holder.netheriteEnchant?.copy() ?: Enchantment.Unset(holder).apply { isNetheriteEnchant = true }
    val enchantments = holder.enchantments?.map { it.copy() }?.toMutableStateList()
        ?: List(9) { Enchantment.Unset(holder) }.toMutableStateList()

    var selected by mutableStateOf(enchantments.getOrNull(holder.enchantments?.indexOf(initialSelected) ?: -1) ?: netherite)
}

@Composable
private fun rememberEnchantmentDataCollectionState(original: Enchantment) =
    remember(original) { EnchantmentDataCollectionState(original) }

@Composable
fun AnimatedVisibilityScope?.EnchantmentOverlay(
    initialSelected: Enchantment,
    requestClose: () -> Unit
) {
    val density = LocalDensity.current
    val windowState = LocalWindowState.current

    val parentHeight = (windowState.size.height - DetailHeight) / 2f
    val childOffset = parentHeight / 2f - DetailHeight / 2f - 25.dp

    val state = rememberEnchantmentDataCollectionState(initialSelected)

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
                EnchantmentDetail(preview)
                Row(modifier = Modifier.offsetRelative(x = 0f, y = 1f).offset(y = -childOffset)) {
                    Spacer(modifier = Modifier.weight(1f))
                    RetroButton(
                        text = Localizations.UiText("cancel"),
                        color = Color.White,
                        hoverInteraction = RetroButtonHoverInteraction.Overlay,
                        onClick = { requestClose() },
                        modifier = Modifier.size(125.dp, 55.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    RetroButton(
                        text = Localizations.UiText("ok"),
                        color = Color(0xff3f8e4f),
                        hoverInteraction = RetroButtonHoverInteraction.Outline,
                        onClick = {
                            state.holder.enchantments = state.enchantments
                            state.holder.netheriteEnchant = state.netherite

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
    offset: Dp,
    modifier: Modifier = Modifier,
) {
    val holder = state.holder

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
                    image = holder.data.largeIcon,
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
                    ItemRarityButton(data = holder.data, rarity = holder.rarity, readonly = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    ItemNetheriteEnchantButton(
                        holder = holder,
                        modifier = Modifier.drawBehind {
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
                        text = holder.data.name,
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
                            text = "${DungeonsPower.toInGamePower(holder.power).roundToInt()}",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.drawBehind { drawEnchantmentRune(topOffset = (-150).dp) }) {
            val slots = remember { state.enchantments.chunked(3).map { EnchantmentsHolder(it) } }
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
                        targetState = each.data,
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
                state.enchantments.all { it.data.id != data.id }
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
    data: EnchantmentData,
    enabled: Boolean,
    selected: Boolean,
    onItemSelect: (EnchantmentData) -> Unit
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
            text = Localizations.UiText("effect_delete"),
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
private fun EnchantmentDetail(enchantment: Enchantment) {
    val data = remember(enchantment.id) { DungeonsDatabase.enchantments.first { it.id == enchantment.id } }
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
                val initialIndex = DungeonsDatabase.enchantments.indexOf(initialState)
                val targetIndex = DungeonsDatabase.enchantments.indexOf(targetState)

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
                ValidEnchantmentPreview(capturedData, enchantment)
            }
        }
    }
}

@Composable
private fun UnsetEnchantmentPreview() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text(
            text = Localizations.UiText("enchantment_empty_slot"),
            modifier = Modifier.alpha(0.75f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ValidEnchantmentPreview(
    capturedData: EnchantmentData,
    parent: Enchantment
) {
    Row {
        Box(modifier = Modifier.fillMaxHeight().aspectRatio(1f / 1f)) {
            EnchantmentImage(
                data = capturedData,
                enabled = false,
                modifier = Modifier.fillMaxSize()
            )
            EnchantmentLevel(level = parent.level)
        }
        Column(modifier = Modifier.padding(end = 36.dp).padding(vertical = 36.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                EnchantmentNameText(capturedData.name)
                if (capturedData.powerful)
                    PowerfulEnchantmentIndicator()
            }

            IfNotNull(capturedData.description) {
                Text(text = it, fontSize = 16.sp, color = Color.White.copy(0.75f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            IfNotNull(capturedData.effect) {
                Text(text = it, fontSize = 20.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))
            EnchantmentLevelSelector(parent)
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
private fun EnchantmentLevelSelector(enchantment: Enchantment) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!enchantment.isNetheriteEnchant)
            EnchantmentDeactivateItem(enchantment)
        EnchantmentLevelSelectorItem(enchantment, 1)
        EnchantmentLevelSelectorItem(enchantment, 2)
        EnchantmentLevelSelectorItem(enchantment, 3)
    }
}

@Composable
private fun PowerfulEnchantmentIndicator() =
    Text(
        text = DungeonsLocalizations["/enchantment_rarity_powerful"]!!,
        style = TextStyle(color = Color(0xffe5247e), fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(start = 10.dp, bottom = 3.dp)
    )

private fun LevelImage(level: Int) =
    if (level == 0) DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment/behind_enchantments_whole_switch.png"]
    else DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${level}_normal_text.png"]

@Composable
private fun RowScope.EnchantmentDeactivateItem(enchantment: Enchantment) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = LevelModifier(enchantment, 0, Modifier.weight(1.75f))
    ) {
        Text(text = Localizations.UiText("enchantment_deactivate"), fontSize = 18.sp)
    }

@Composable
private fun RowScope.EnchantmentLevelSelectorItem(enchantment: Enchantment, level: Int) =
    Image(
        bitmap = LevelImage(level),
        contentDescription = null,
        modifier = LevelModifier(enchantment, level, Modifier.weight(1f))
    )

@Composable
private fun LevelModifier(
    enchantment: Enchantment,
    level: Int,
    modifier: Modifier = Modifier
): Modifier {
    val interaction = rememberMutableInteractionSource()
    val pressed by interaction.collectIsPressedAsState()
    val hovered by interaction.collectIsHoveredAsState()

    return Modifier
        .padding(start = if (level != 0) 4.dp else 0.dp)
        .height(42.dp)
        .hoverable(interaction)
        .clickable(interaction, null) { enchantment.applyInvestedPoints(level) }
        .drawWithCache {
            val indicator = RetroIndicator()
            val baseColor = if (level == 0) Color(0xfff54242) else Color(0xff6642f5)
            val color =
                if (enchantment.level == level) baseColor.copy(alpha = if (pressed) 0.6f else if (hovered) 0.8f else 1f)
                else Color.White.copy(alpha = if (pressed) 0.1f else if (hovered) 0.2f else 0f)

            onDrawBehind {
                drawPath(
                    path = indicator,
                    color = color
                )
            }
        }
        .scale(if (level == 0) 1f else 2f)
        .then(modifier)
}
