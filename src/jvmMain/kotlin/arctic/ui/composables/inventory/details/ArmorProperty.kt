package arctic.ui.composables.inventory.details

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import arctic.states.Arctic
import arctic.states.ItemArmorPropertyOverlayState
import arctic.ui.composables.atomic.densityDp
import arctic.ui.composables.atomic.rememberArmorPropertyIconAsState
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import arctic.ui.utils.rememberMutableInteractionSource
import dungeons.states.ArmorProperty
import dungeons.states.Item

@Composable
fun ItemArmorProperties(item: Item, properties: List<ArmorProperty>) {
    val groupedProperties by remember {
        derivedStateOf {
            val sorted = properties.sortedBy { it.data.description?.length }
            val uniques = sorted.filter { it.rarity.lowercase() == "unique" }
            val commons = sorted.filter { it.rarity.lowercase() == "common" }
            val groupedUniques = groupByLength(uniques)
            val groupedCommons = groupByLength(commons)
            mutableListOf<List<ArmorProperty>>()
                .apply {
                    this.addAll(groupedUniques)
                    this.addAll(groupedCommons)
                }
                .toList()
        }
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            for (propertyRow in groupedProperties) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (property in propertyRow) {
                        ArmorPropertyItem(item, property)
                        if (propertyRow.indexOf(property) == 0) Spacer(modifier = Modifier.width(20.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        ArmorPropertyAddButton(item = item)
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun RowScope.ArmorPropertyItem(item: Item, property: ArmorProperty) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    val propertyRarityIcon by rememberArmorPropertyIconAsState(property)

    val overlayState = Arctic.overlayState

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .weight(1f)
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = if (hovered) 0.1f else 0.0f),
                    topLeft = Offset(densityDp(-10), 0f),
                    size = Size(size.width + densityDp(20), size.height),
                    cornerRadius = CornerRadius(densityDp(6))
                )
            }
            .hoverable(interaction)
            .clickable(interaction, null) { overlayState.armorProperty = ItemArmorPropertyOverlayState(item, property) }
    ) {
        Image(
            bitmap = propertyRarityIcon,
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = property.data.description ?: property.id,
            fontSize = 25.sp,
            color = Color.White
        )
    }
}

@Composable
private fun ArmorPropertyAddButton(item: Item) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    Spacer(
        modifier = Modifier
            .size(35.dp)
            .drawBehind {
                if (hovered) drawRoundRect(Color.White, alpha = 0.15f, cornerRadius = CornerRadius(densityDp(6)))
                drawRect(Color.White, topLeft = Offset(size.width / 2 - densityDp(2), densityDp(8)), size = Size(densityDp(4), size.height - densityDp(16)))
                drawRect(Color.White, topLeft = Offset(densityDp(8), size.height / 2 - densityDp(2)), size = Size(size.width - densityDp(16), densityDp(4)))
            }
            .hoverable(interaction)
            .clickable(interaction, null) { Arctic.overlayState.armorProperty = ItemArmorPropertyOverlayState(item) }
    )
}

private fun groupByLength(input: List<ArmorProperty>): List<List<ArmorProperty>> {
    val result = mutableListOf<MutableList<ArmorProperty>>(mutableListOf())
    input.forEach {
        val description = it.data.description
        val long = (description?.length ?: it.data.id.length) > 12
        if (!long) {
            if (result.last().size == 2) result.add(mutableListOf(it))
            else result.last().add(it)
        } else {
            result.add(mutableListOf(it))
        }
    }
    return result
}