package arctic.ui.composables.overlays.extended

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import arctic.states.arctic
import arctic.ui.composables.atomic.ArmorPropertyRarityIcon
import arctic.ui.composables.overlays.OverlayBackdrop
import arctic.ui.utils.rememberMutableInteractionSource
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.ArmorPropertyData
import dungeons.Database
import dungeons.states.ArmorProperty
import dungeons.states.Item
import dungeons.states.extensions.data
import extensions.replace

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

            if (holder != null) ArmorPropertyDataCollection(holder, index, indexValid)
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

@Composable
private fun ArmorPropertyDataCollection(holder: Item, index: Int?, indexValid: Boolean) {
    val property = if (index != null && indexValid) holder.armorProperties?.get(index) else null

    val datasets = remember { Database.armorProperties.filter { it.description != null }.sortedBy { it.id } }

    val initialFirstVisibleItemIndex = if (property != null) datasets.indexOfFirst { it.id == property.id }.coerceAtLeast(0) else 0
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialFirstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = -602.dp.value.toInt()
    )

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 30.dp),
        modifier = Modifier
            .requiredWidth(700.dp)
            .fillMaxHeight()
            .background(Color(0xff080808))
    ) {
        items(datasets, key = { it.id }) { data ->
            ArmorPropertyCollectionItem(holder, index, indexValid, data)
        }
    }
}

@Composable
private fun ArmorPropertyCollectionItem(
    holder: Item,
    index: Int?,
    indexValid: Boolean,
    data: ArmorPropertyData
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val selected = index != null && indexValid && holder.armorProperties?.get(index)?.id == data.id

    val onItemClick: () -> Unit = {
        val properties = holder.armorProperties!!

        val newProperty = ArmorProperty(holder, data.id)

        if (index != null && indexValid) {
            val existing = properties[index]
            if (existing.id == newProperty.id) {
                properties.remove(existing)
                arctic.armorProperties.closeDetail()
                arctic.armorProperties.requestCreate(existing.holder)
            } else {
                properties.replace(properties[index], newProperty)
                arctic.armorProperties.viewDetail(newProperty)
            }
        } else {
            properties.add(newProperty)
            arctic.armorProperties.viewDetail(newProperty)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interaction)
            .clickable(interaction, null, onClick = onItemClick)
            .background(Color.White.copy(alpha = if (selected) 0.3f else if (hovered) 0.15f else 0f))
            .padding(vertical = 10.dp, horizontal = 30.dp)
    ) {
        Text(text = data.description!!, fontSize = 26.sp, color = Color.White)
        Text(text = data.id, fontSize = 18.sp, color = Color.White)
    }
}

@Composable
private fun ArmorPropertyDetail(property: ArmorProperty) {
    Column(
        modifier = Modifier
            // .wrapContentSize()
            .requiredWidth(675.dp)
            .background(Color(0xff080808))
            .padding(30.dp)
    ) {
        Text(text = "방어구 속성", fontSize = 18.sp, color = Color.White)
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ArmorPropertyRarityToggle(property)
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = property.data.description!!,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ArmorPropertyRarityToggle(property: ArmorProperty) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Image(
        bitmap = ArmorPropertyRarityIcon(property.rarity),
        contentDescription = null,
        modifier = Modifier
            .size(41.dp)
            .offset(y = 1.5.dp)
            .hoverable(interaction)
            .clickable(interaction, null) { property.rarity = if (property.rarity == "Common") "Unique" else "Common" }
            .background(Color.White.copy(if (hovered) 0.3f else 0f), shape = RoundedCornerShape(6.dp.value))
            .padding(3.dp)
    )
}
