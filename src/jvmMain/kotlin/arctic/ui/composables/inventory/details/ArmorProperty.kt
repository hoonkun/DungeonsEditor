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
import arctic.states.arctic
import arctic.ui.composables.atomic.ArmorPropertyRarityIcon
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import arctic.ui.utils.rememberMutableInteractionSource
import dungeons.states.ArmorProperty
import dungeons.states.Item
import dungeons.states.extensions.data

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
                        ArmorPropertyItem(property)
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
private fun RowScope.ArmorPropertyItem(property: ArmorProperty) {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val selected = arctic.armorProperties.detailTarget == property

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .weight(1f)
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = if (selected) 0.2f else if (hovered) 0.1f else 0.0f),
                    topLeft = Offset(-10.dp.value, 0f),
                    size = Size(size.width + 20.dp.value, size.height),
                    cornerRadius = CornerRadius(6.dp.value, 6.dp.value)
                )
            }
            .hoverable(interaction)
            .clickable(interaction, null) { arctic.armorProperties.viewDetail(property) }
    ) {
        Image(
            bitmap = ArmorPropertyRarityIcon(property.rarity),
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
                if (hovered) drawRoundRect(Color.White, alpha = 0.15f, cornerRadius = CornerRadius(6.dp.value, 6.dp.value))
                drawRect(Color.White, topLeft = Offset(size.width / 2 - 2f, 8f), size = Size(4f, size.height - 16f))
                drawRect(Color.White, topLeft = Offset(8f, size.height / 2 - 2f), size = Size(size.width - 16f, 4f))
            }
            .hoverable(interaction)
            .clickable(interaction, null) { arctic.armorProperties.requestCreate(item) }
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