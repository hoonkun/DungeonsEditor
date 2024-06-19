package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.ui.composables.base.AutosizeText
import kiwi.hoonkun.ui.composables.base.EnchantmentIconImage
import kiwi.hoonkun.ui.composables.base.EnchantmentLevelImage
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.Enchantment
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsDatabase
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.resources.EnchantmentData


@Stable
class EnchantmentDataCollectionState(val initialSelected: Enchantment) {
    companion object {
        fun new(holder: Item) = Enchantment.Unset(holder)
    }

    var selected by mutableStateOf(initialSelected.copy())
}

@Composable
fun rememberEnchantmentDataCollectionState(original: Enchantment) =
    remember { EnchantmentDataCollectionState(original) }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedVisibilityScope.EnchantmentOverlay(original: Enchantment) {
    val state = rememberEnchantmentDataCollectionState(original)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        EnchantmentDataCollection(
            state = state,
            modifier = Modifier
                .animateEnterExit(
                    enter = slideIn { IntOffset(-60.dp.value.toInt(), 0) },
                    exit = ExitTransition.None
                )
        )
        Spacer(modifier = Modifier.width(40.dp))
        AnimatedContent(
            targetState = state.selected,
            transitionSpec = {
                val enter =
                    if (initialState.id == "Unset") defaultFadeIn()
                    else defaultFadeIn() + defaultSlideIn { IntOffset(50.dp.value.toInt(), 0) }
                val exit =
                    if (targetState.id == "Unset") defaultFadeOut()
                    else defaultFadeOut() + defaultSlideOut { IntOffset(50.dp.value.toInt(), 0) }

                enter togetherWith exit using SizeTransform(clip = false)
            },
            modifier = Modifier
                .height(500.dp)
                .animateEnterExit(
                    enter = slideIn { IntOffset(0, 60.dp.value.toInt()) },
                    exit = ExitTransition.None
                )
        ) { preview ->
            if (preview.id != "Unset") EnchantmentDetail(preview)
            else Box(modifier = Modifier.width(0.dp).height(500.dp))
        }
    }
}

@Composable
private fun EnchantmentDataCollection(
    state: EnchantmentDataCollectionState,
    modifier: Modifier = Modifier
) {
    val initialSelected = state.initialSelected
    val variant = initialSelected.holder.data.variant

    val datasets = remember(variant) {
        DungeonsDatabase.enchantments.filter { it.applyFor.contains(variant) }
    }

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = remember {
            datasets.indexOfFirst { it.id == state.selected.id }.coerceAtLeast(0)
        },
        initialFirstVisibleItemScrollOffset = -529.dp.value.toInt()
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
        items(datasets, key = { it.id }) { data ->
            val isUniqueInHolder = remember(data, state.selected) {
                state.selected.holder.enchantments?.all { it.data.id != data.id } ?: true
            }

            EnchantmentDataCollectionItem(
                data = data,
                enabled = initialSelected.id == data.id || data.stackable || isUniqueInHolder,
                selected = state.selected.id == data.id,
                onItemSelect = { newData ->
                    val newId = if (newData.id == state.selected.id) "Unset" else newData.id
                    state.selected = state.selected.copy(
                        id = newId,
                        level =
                            if (newId == "Unset")
                                0
                            else if (state.selected.id == "Unset" && initialSelected.isNetheriteEnchant)
                                initialSelected.level.coerceAtLeast(1)
                            else
                                initialSelected.level
                    )

                    // TODO: 저장 시로 아래 로직을 옮길 것
//                    state.selected.applyHolderInvestedPoints()
//                    if (preview.isNetheriteEnchant) {
//                        holder.netheriteEnchant = newEnchantment
//                    } else {
//                        holder.enchantments?.replace(applyTarget, newEnchantment)
//                    }
//
//                    holder.updateEnchantmentInvestedPoints()
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        EnchantmentIconImage(
            data = data,
            selected = selected,
            disableInteraction = !enabled,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f / 1f),
            onClick = { onItemSelect(data) }
        )
        AutosizeText(
            text = data.name,
            style = LocalTextStyle.current.copy(
                color = Color.White.copy(alpha = if (enabled || selected) 1f else 0.5f),
                fontWeight = if (enabled || selected) FontWeight.Bold else FontWeight.Normal,
            ),
            maxFontSize = 19.sp,
            modifier = Modifier.offset(y = (-12).dp)
        )
    }
}


@Composable
private fun EnchantmentDetail(enchantment: Enchantment) {
    Row(modifier = Modifier.requiredSize(675.dp, 300.dp).background(Color(0xff080808))) {
        Box(modifier = Modifier.fillMaxHeight().aspectRatio(1f / 1f)) {
            EnchantmentIconImage(
                data = enchantment.data,
                hideIndicator = true,
                modifier = Modifier.fillMaxSize()
            )
            EnchantmentLevelImage(level = enchantment.level)
        }
        Column(modifier = Modifier.padding(top = 20.dp, end = 30.dp, bottom = 30.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                EnchantmentNameText(enchantment.data.name, wide = !enchantment.data.powerful)
                if (enchantment.data.powerful) PowerfulEnchantmentIndicator()
            }

            val description = enchantment.data.description
            if (description != null)
                Text(text = description, fontSize = 18.sp, color = Color.White)

            Spacer(modifier = Modifier.height(20.dp))

            val effect = enchantment.data.effect
            if (effect != null)
                Text(text = effect, fontSize = 20.sp, color = Color.White)

            if (enchantment.id != "Unset") {
                Spacer(modifier = Modifier.weight(1f))
                EnchantmentLevelSelector(enchantment)
            }
        }
    }
}

@Composable
private fun EnchantmentNameText(text: String, wide: Boolean) =
    AutosizeText(
        text = text,
        maxFontSize = 40.sp,
        style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.requiredWidthIn(max = if (wide) Dp.Companion.Unspecified else 250.dp)
    )

@Composable
private fun EnchantmentLevelSelector(enchantment: Enchantment) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!enchantment.isNetheriteEnchant)
            EnchantmentLevelSelectorItem(enchantment, 0)
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
private fun RowScope.EnchantmentLevelSelectorItem(enchantment: Enchantment, level: Int) {
    val interaction = rememberMutableInteractionSource()

    Image(
        bitmap = LevelImage(level),
        contentDescription = null,
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .background(
                color =
                if (enchantment.level == level) Color(if (level == 0) 0x20ffffff else 0x20b442f6)
                else Color.Transparent,
                shape = RoundedCornerShape(5.dp)
            )
            .clickable(interaction, null) { enchantment.applyHolderInvestedPoints(level) }
            .scale(if (level == 0) 0.9f else 2f)
    )
}
