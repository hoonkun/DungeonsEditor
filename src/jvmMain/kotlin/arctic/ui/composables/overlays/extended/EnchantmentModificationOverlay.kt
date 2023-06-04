package arctic.ui.composables.overlays.extended

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import arctic.states.arctic
import arctic.ui.composables.overlays.OverlayBackdrop
import arctic.ui.unit.dp
import composable.blackstone.popup.EnchantmentDetail
import composable.blackstone.popup.EnchantmentsCollection
import dungeons.states.Enchantment
import dungeons.states.Item

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
            if (holder != null && index != null) EnchantmentsCollection(holder, index, isNetheriteEnchant)
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
