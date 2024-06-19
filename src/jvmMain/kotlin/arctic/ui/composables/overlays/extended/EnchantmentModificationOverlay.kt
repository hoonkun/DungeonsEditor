package arctic.ui.composables.overlays.extended

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import arctic.states.Arctic
import arctic.states.ItemEnchantmentOverlayState
import arctic.ui.composables.atomic.AutosizeText
import arctic.ui.composables.atomic.EnchantmentIconImage
import arctic.ui.composables.atomic.EnchantmentLevelImage
import arctic.ui.composables.atomic.PowerfulEnchantmentIndicator
import arctic.ui.composables.overlays.OverlayBackdrop
import arctic.ui.composables.overlays.SizeMeasureDummy
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import arctic.ui.utils.rememberMutableInteractionSource
import dungeons.Database
import dungeons.EnchantmentData
import dungeons.IngameImages
import dungeons.states.Enchantment
import dungeons.states.Item
import dungeons.states.extensions.leveling
import kiwi.hoonkun.utils.replace

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EnchantmentModificationOverlay() {
    val enchantmentOverlay = Arctic.overlayState.enchantment

    OverlayBackdrop(enchantmentOverlay != null) { Arctic.overlayState.enchantment = null }
    AnimatedContent(
        targetState = enchantmentOverlay,
        transitionSpec = { fadeIn() with fadeOut() using SizeTransform(false) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (it != null)
            EnchantmentModificationOverlayContent(
                enchantmentOverlay = it,
                collectionModifier = Modifier.animateEnterExit(enter = slideIn { IntOffset(-60.dp.value.toInt(), 0) }, exit = ExitTransition.None),
                previewModifier = Modifier.animateEnterExit(enter = slideIn { IntOffset(0, 60.dp.value.toInt()) }, exit = ExitTransition.None)
            )
        else SizeMeasureDummy()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EnchantmentModificationOverlayContent(
    enchantmentOverlay: ItemEnchantmentOverlayState,
    collectionModifier: Modifier = Modifier,
    previewModifier: Modifier = Modifier
) {
    val preview = enchantmentOverlay.preview
    val target = enchantmentOverlay.target

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        EnchantmentDataCollection(
            holder = target,
            applyTarget = preview,
            enchantmentOverlay = enchantmentOverlay,
            modifier = collectionModifier
        )
        Spacer(modifier = Modifier.width(40.dp))
        AnimatedContent(
            targetState = preview,
            transitionSpec = OverlayTransitions.detail(slideEnabled = { i, t -> i.id != "Unset" && t.id != "Unset" }),
            modifier = Modifier.height(500.dp).then(previewModifier)
        ) { preview ->
            if (preview.id != "Unset") EnchantmentDetail(preview)
            else Box(modifier = Modifier.width(0.dp).height(500.dp))
        }
    }
}

@Composable
private fun EnchantmentDataCollection(holder: Item, applyTarget: Enchantment, enchantmentOverlay: ItemEnchantmentOverlayState, modifier: Modifier) {
    val datasets = remember(holder.data.variant) {
        Database.enchantments.filter { it.applyFor?.contains(holder.data.variant) == true }
    }

    val initialFirstVisibleItemIndex = remember(applyTarget.id) { datasets.indexOfFirst { it.id == applyTarget.id }.coerceAtLeast(0)}
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = initialFirstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = -529.dp.value.toInt()
    )

    val onItemSelected: (EnchantmentData) -> Unit = { newData ->
        val newId = if (newData.id == applyTarget.id) "Unset" else newData.id
        val newEnchantment = Enchantment(
            holder = holder,
            id = newId,
            level =
                if (newId == "Unset") 0
                else if (applyTarget.id == "Unset" && applyTarget.isNetheriteEnchant) 1
                else applyTarget.level,
            isNetheriteEnchant = applyTarget.isNetheriteEnchant
        )
        newEnchantment.leveling()

        if (applyTarget.isNetheriteEnchant) {
            holder.netheriteEnchant = newEnchantment
        } else {
            holder.enchantments?.replace(applyTarget, newEnchantment)
        }

        holder.updateEnchantmentInvestedPoints()
        enchantmentOverlay.preview = newEnchantment
    }

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
            EnchantmentDataCollectionItem(
                data = data,
                enabled = data.multipleAllowed || (holder.enchantments?.all { it.id != data.id } ?: true),
                selected = applyTarget.id == data.id,
                onItemSelect = onItemSelected
            )
        }
    }
}

@Composable
private fun EnchantmentDataCollectionItem(data: EnchantmentData, enabled: Boolean, selected: Boolean, onItemSelect: (EnchantmentData) -> Unit) {
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
            color = Color.White.copy(alpha = if (enabled || selected) 1f else 0.5f),
            maxFontSize = 19.sp,
            fontWeight = if (enabled || selected) FontWeight.Bold else FontWeight.Normal,
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
        fontWeight = FontWeight.Bold,
        color = Color.White,
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


private val ZeroLevelImage get() = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment/behind_enchantments_whole_switch.png" }
private fun LevelImage(level: Int) = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${level}_normal_text.png" }

@Composable
private fun RowScope.EnchantmentLevelSelectorItem(enchantment: Enchantment, level: Int) {
    val interaction = rememberMutableInteractionSource()

    Image(
        bitmap = if (level == 0) ZeroLevelImage else LevelImage(level),
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
            .clickable(interaction, null) { enchantment.leveling(level) }
            .scale(if (level == 0) 0.9f else 2f)
    )
}
