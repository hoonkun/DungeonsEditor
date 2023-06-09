package arctic.ui.composables.overlays.extended

import androidx.compose.animation.*
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
import androidx.compose.ui.unit.IntOffset
import arctic.states.Arctic
import arctic.states.ItemArmorPropertyOverlayState
import arctic.ui.composables.atomic.rememberArmorPropertyIconAsState
import arctic.ui.composables.overlays.OverlayBackdrop
import arctic.ui.composables.overlays.SizeMeasureDummy
import arctic.ui.utils.rememberMutableInteractionSource
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.ArmorPropertyData
import dungeons.Database
import dungeons.states.ArmorProperty
import dungeons.states.Item
import extensions.replace

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ArmorPropertyModificationOverlay() {
    val armorPropertyOverlay = Arctic.overlayState.armorProperty

    OverlayBackdrop(armorPropertyOverlay != null) { Arctic.overlayState.armorProperty = null }
    AnimatedContent(
        targetState = armorPropertyOverlay,
        transitionSpec = { fadeIn() with fadeOut() using SizeTransform(false) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (it != null)
            ArmorPropertyModificationOverlayContent(
                it,
                collectionModifier = Modifier.animateEnterExit(enter = slideIn { IntOffset(-60.dp.value.toInt(), 0) }, exit = ExitTransition.None),
                previewModifier = Modifier.animateEnterExit(enter = slideIn { IntOffset(0, 60.dp.value.toInt()) }, exit = ExitTransition.None)
            )
        else SizeMeasureDummy()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ArmorPropertyModificationOverlayContent(
    armorPropertyOverlay: ItemArmorPropertyOverlayState,
    collectionModifier: Modifier = Modifier,
    previewModifier: Modifier = Modifier
) {
    val preview = armorPropertyOverlay.preview
    val target = armorPropertyOverlay.target

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        ArmorPropertyDataCollection(target, preview, armorPropertyOverlay, modifier = collectionModifier)
        Spacer(modifier = Modifier.width(40.dp))
        AnimatedContent(
            targetState = preview,
            transitionSpec = OverlayTransitions.detail(slideEnabled = { i, t -> i != null && t != null }),
            modifier = Modifier.height(500.dp).then(previewModifier)
        ) { preview ->
            if (preview != null) ArmorPropertyDetail(preview)
            else Box(modifier = Modifier.size(0.dp, 500.dp))
        }
    }
}

@Composable
private fun ArmorPropertyDataCollection(holder: Item, property: ArmorProperty?, armorPropertyOverlay: ItemArmorPropertyOverlayState, modifier: Modifier = Modifier) {
    val datasets = remember { Database.armorProperties.filter { it.description != null }.sortedBy { it.id } }

    val initialFirstVisibleItemIndex = remember(datasets, property) {
        if (property != null) datasets.indexOfFirst { it.id == property.id }.coerceAtLeast(0) else 0
    }
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
            .then(modifier)
            .background(Color(0xff080808))
    ) {
        items(datasets, key = { it.id }) { data ->
            ArmorPropertyCollectionItem(holder, data, property, armorPropertyOverlay)
        }
    }
}

@Composable
private fun ArmorPropertyCollectionItem(
    holder: Item,
    data: ArmorPropertyData,
    replaceFrom: ArmorProperty?,
    armorPropertyOverlay: ItemArmorPropertyOverlayState
) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val selected = replaceFrom != null && replaceFrom.id == data.id

    val onItemClick: () -> Unit = {
        val properties = holder.armorProperties!!

        val newProperty = ArmorProperty(holder, data.id)

        if (replaceFrom != null) {
            if (replaceFrom.id == data.id) {
                properties.remove(replaceFrom)
                armorPropertyOverlay.preview = null
            } else {
                properties.replace(replaceFrom, newProperty)
                armorPropertyOverlay.preview = newProperty
            }
        } else {
            properties.add(newProperty)
            armorPropertyOverlay.preview = newProperty
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
            .wrapContentSize()
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

    val rarityIcon by rememberArmorPropertyIconAsState(property)

    Image(
        bitmap = rarityIcon,
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
