package arctic.ui.composables.overlays.extended

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import arctic.states.arctic
import arctic.ui.composables.overlays.OverlayBackdrop
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import composable.blackstone.popup.ItemDataCollection
import composable.blackstone.popup.ItemDataDetail
import dungeons.ItemData
import dungeons.states.extensions.data


@Composable
fun ItemCreationOverlay() {
    ItemDataCollectionOverlay(
        targetState = arctic.creation.enabled.takeIf { it },
        variant = { arctic.creation.filter },
        blur = arctic.creation.target != null,
        onExit = { arctic.creation.disable() },
        onSelect = { arctic.creation.target = it }
    )

    ItemDataDetailOverlay(
        target = arctic.creation.target
    )
}

@Composable
fun ItemEditionOverlay() {
    ItemDataCollectionOverlay(
        targetState = arctic.edition.target,
        variant = { it.data.variant },
        onExit = { arctic.edition.disable() },
        onSelect = {
            arctic.edition.target?.type = it.type
            arctic.edition.disable()
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S>ItemDataCollectionOverlay(
    targetState: S?,
    variant: (S) -> String,
    filterEnabled: Boolean = true,
    blur: Boolean = false,
    onExit: () -> Unit,
    onSelect: (ItemData) -> Unit
) {
    val enabled = targetState != null
    val blurRadius by animateDpAsState(if (blur) 75.dp else 0.dp)

    OverlayBackdrop(enabled, onClick = onExit)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().blur(blurRadius)
    ) {
        AnimatedContent(
            targetState = targetState,
            transitionSpec = OverlayTransitions.collection(),
            modifier = Modifier.width(160.dp).padding(top = 54.dp)
        ) { targetState ->
            if (targetState != null) ItemCreationVariantFilters(if (filterEnabled) null else variant(targetState))
            else Spacer(modifier = Modifier.requiredWidth(160.dp).fillMaxHeight())
        }
        AnimatedContent(
            targetState = targetState to variant,
            transitionSpec = OverlayTransitions.collection(),
            modifier = Modifier.width(1050.dp)
        ) { (targetState, variant) ->
            if (targetState != null) ItemDataCollection(variant(targetState), onItemSelect = onSelect)
            else Box(modifier = Modifier.requiredWidth(1050.dp).fillMaxHeight())
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ItemDataDetailOverlay(target: ItemData?) {
    OverlayBackdrop(target != null) { arctic.creation.target = null }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedContent(
            targetState = target,
            transitionSpec = OverlayTransitions.detail(),
            modifier = Modifier.fillMaxWidth().height(550.dp)
        ) { target ->
            if (target != null) ItemDataDetail(target)
            else Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ItemCreationVariantFilters(fixed: String?) {
    Column(horizontalAlignment = Alignment.End, modifier = Modifier.requiredWidth(160.dp).padding(top = 54.dp)) {
        ItemCreationVariantFilter("근거리", "Melee", fixed == null || fixed == "Melee")
        ItemCreationVariantFilter("원거리", "Ranged", fixed == null || fixed == "Ranged")
        ItemCreationVariantFilter("방어구", "Armor", fixed == null || fixed == "Armor")
        ItemCreationVariantFilter("유물", "Artifact", fixed == null || fixed == "Artifact")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemCreationVariantFilter(text: String, variant: String, enabled: Boolean = true) {
    val selected = arctic.creation.filter == variant
    val onClick = { arctic.creation.filter = variant }

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    val blurAlpha by animateFloatAsState(if (!enabled) 0f else if (hovered) 0.8f else 0f)
    val overlayAlpha by animateFloatAsState(if (!enabled) 0.15f else if (selected) 1f else 0.6f)

    Box(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .hoverable(source, enabled = enabled)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary), enabled = enabled, onClick = onClick)
    ) {
        VariantFilterText(text, modifier = Modifier.blur(10.dp).alpha(blurAlpha))
        VariantFilterText(text, modifier = Modifier.alpha(overlayAlpha))
    }
}

@Composable
fun VariantFilterText(text: String, modifier: Modifier = Modifier) =
    Text(
        text = text,
        color = Color.White,
        fontSize = 35.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 20.dp, top = 5.dp, bottom = 5.dp, end = 20.dp)
    )