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
import composable.blackstone.popup.ArmorPropertyCollection
import composable.blackstone.popup.ArmorPropertyDetail
import dungeons.states.ArmorProperty
import dungeons.states.Item

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ArmorPropertyModificationOverlay() {
    val target = arctic.armorProperties.detailTarget
    val into = arctic.armorProperties.createInto
    val created = arctic.armorProperties.created

    val hasTarget = target != null
    val hasInto = into != null

    val collectionTargetStates =
        if (hasInto)
            CollectionTargetStatesA(holder = into, index = into?.armorProperties?.size)
        else
            CollectionTargetStatesA(holder = target?.holder, index = target?.holder?.armorProperties?.indexOf(target))
    val detailTargetStates = DetailTargetStatesA(
        holder = collectionTargetStates.holder,
        target = target,
        created = created
    )

    val slideEnabled: (DetailTargetStatesA, DetailTargetStatesA) -> Boolean = { initialState, targetState ->
        initialState.target != null && targetState.holder != null && targetState.target != null || initialState.holder == null && targetState.holder != null
    }
    val sizeTransformDuration: (DetailTargetStatesA, DetailTargetStatesA) -> Int = { initialState, targetState ->
        if ((initialState.holder == null && initialState.created && !targetState.created) || (initialState.holder == null && initialState.target == null && targetState.created)) 0
        else 250
    }

    OverlayBackdrop(hasTarget || hasInto) {
        if (hasTarget) arctic.armorProperties.closeDetail()
        else if (hasInto) arctic.armorProperties.cancelCreation()
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = collectionTargetStates,
            transitionSpec = OverlayTransitions.collection(),
            modifier = Modifier.width(750.dp)
        ) { (holder, index) ->
            val indexValid = index != null && index < (holder?.armorProperties?.size ?: 0)

            if (holder != null) ArmorPropertyCollection(holder, index, indexValid)
            else Box(modifier = Modifier.width(700.dp).fillMaxHeight())
        }
        AnimatedContent(
            targetState = detailTargetStates,
            transitionSpec = OverlayTransitions.detail(slideEnabled, sizeTransformDuration),
            modifier = Modifier.height(500.dp)
        ) {
            if (target != null) ArmorPropertyDetail(target)
            else Box(modifier = Modifier.size(0.dp, 500.dp))

            if (target == null && created) {
                Box(modifier = Modifier.width(675.dp).height(500.dp))
            } else if (target == null) {
                Box(modifier = Modifier.width(0.dp).height(500.dp))
            }
        }
    }
}

private data class CollectionTargetStatesA(
    val holder: Item?,
    val index: Int?
)

private data class DetailTargetStatesA(
    val holder: Item?,
    val target: ArmorProperty?,
    val created: Boolean
)
