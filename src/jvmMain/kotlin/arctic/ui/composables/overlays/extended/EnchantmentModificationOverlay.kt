package arctic.ui.composables.overlays.extended

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import arctic.states.arctic
import arctic.ui.composables.atomic.*
import arctic.ui.composables.overlays.OverlayBackdrop
import arctic.ui.composables.rememberMutableInteractionSource
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.Database
import dungeons.EnchantmentData
import dungeons.IngameImages
import dungeons.states.Enchantment
import dungeons.states.Item
import dungeons.states.extensions.data
import dungeons.states.extensions.leveling
import dungeons.states.extensions.updateEnchantmentInvestedPoints
import extensions.replace

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EnchantmentModificationOverlay() {
    val target = arctic.enchantments.detailTarget
    val shadow = arctic.enchantments.shadowDetailTarget

    val collectionTargetStates = CollectionTargetStatesE(
        holder = target?.holder,
        index = target?.holder?.enchantments?.indexOf(target),
        isNetheriteEnchant = target?.isNetheriteEnchant == true
    )
    val detailTargetStates = DetailTargetStatesE(
        target = target,
        isUnset = target?.id == "Unset"
    )

    OverlayBackdrop(target != null) { arctic.enchantments.closeDetail() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            collectionTargetStates,
            transitionSpec = OverlayTransitions.collection(),
            modifier = Modifier.width(750.dp)
        ) { (holder, index, isNetheriteEnchant) ->
            if (holder != null && index != null)
                EnchantmentDataCollection(
                    holder = holder,
                    applyTarget =
                        if (isNetheriteEnchant) holder.netheriteEnchant!!
                        else holder.enchantments!![index]
                )
            else Box(modifier = Modifier.requiredWidth(750.dp).fillMaxHeight())
        }
        AnimatedContent(
            detailTargetStates,
            transitionSpec = OverlayTransitions.detail(),
            modifier = Modifier.height(500.dp)
        ) { (target, isUnset) ->
            if (target != null && !isUnset) EnchantmentDetail(target)
            else Box(modifier = Modifier.width(0.dp).height(500.dp))

            if (target == null && shadow != null && shadow.id != "Unset")
                Box(modifier = Modifier.width(675.dp).height(500.dp))
            else if (target == null && shadow != null && shadow.id == "Unset")
                Box(modifier = Modifier.width(0.dp).height(500.dp))
        }
    }
}

private data class CollectionTargetStatesE(
    val holder: Item?,
    val index: Int?,
    val isNetheriteEnchant: Boolean
)

private data class DetailTargetStatesE(
    val target: Enchantment?,
    val isUnset: Boolean
)

@Composable
private fun EnchantmentDataCollection(holder: Item, applyTarget: Enchantment) {
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
                if (applyTarget.id == "Unset" && newId != "Unset" && applyTarget.isNetheriteEnchant) 1
                else if (newId != "Unset") applyTarget.level
                else 0,
            isNetheriteEnchant = applyTarget.isNetheriteEnchant
        )
        newEnchantment.leveling()

        if (applyTarget.isNetheriteEnchant) {
            holder.netheriteEnchant = newEnchantment
        } else {
            holder.enchantments?.replace(applyTarget, newEnchantment)
        }

        holder.updateEnchantmentInvestedPoints()
        arctic.enchantments.viewDetail(newEnchantment)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        state = gridState,
        contentPadding = PaddingValues(vertical = 60.dp, horizontal = 10.dp),
        modifier = Modifier
            .requiredWidth(700.dp)
            .fillMaxHeight()
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
            modifier = Modifier.fillMaxWidth().aspectRatio(1f / 1f),
            onClick = { onItemSelect(data) }
        )
        Text(
            text = data.name,
            color = Color.White.copy(alpha = if (enabled || selected) 1f else 0.5f),
            fontSize = 19.sp,
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
                indicatorEnabled = false,
                modifier = Modifier.fillMaxSize()
            )
            EnchantmentLevelImage(
                level = enchantment.level,
                positionerSize = 0.3f,
                scale = 1.5f
            )
        }
        Column(modifier = Modifier.padding(top = 20.dp, end = 30.dp, bottom = 30.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = enchantment.data.name, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PowerfulEnchantmentIndicator()
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
private fun EnchantmentLevelSelector(enchantment: Enchantment) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!enchantment.isNetheriteEnchant)
            EnchantmentLevelSelectorItem(enchantment, 0)
        EnchantmentLevelSelectorItem(enchantment, 1)
        EnchantmentLevelSelectorItem(enchantment, 2)
        EnchantmentLevelSelectorItem(enchantment, 3)
    }
}


val ZeroLevelImage = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment/behind_enchantments_whole_switch.png" }
fun LevelImage(level: Int) = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment/Inspector2/level_${level}_normal_text.png" }

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
    )
}
