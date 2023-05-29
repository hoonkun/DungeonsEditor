package composable.blackstone.popup

import ArmorPropertyData
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.TextStyle
import blackstone.states.dp
import blackstone.states.sp
import arctic
import blackstone.states.ArmorProperty
import blackstone.states.Item
import blackstone.states.items.ArmorProperty
import extensions.replace

@Composable
fun ArmorPropertyCollection(holder: Item, index: Int?, indexValid: Boolean) {

    val property = if (index != null && indexValid) holder.armorProperties?.get(index) else null

    val sorted = remember { Database.current.armorProperties.filter { it.description != null }.sortedBy { it.description } }
    val lazy = rememberLazyListState(
        initialFirstVisibleItemIndex = if (property != null) sorted.indexOfFirst { it.id == property.id }.coerceAtLeast(0) else 0,
        initialFirstVisibleItemScrollOffset = -602.dp.value.toInt()
    )

    LazyColumn(
        state = lazy,
        contentPadding = PaddingValues(30.dp),
        modifier = Modifier
            .requiredWidth(700.dp)
            .fillMaxHeight()
            .background(Color(0xff080808)),
    ) {
        items(sorted) { propertyData ->
            ArmorPropertySelectText(holder, index, indexValid, propertyData)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArmorPropertySelectText(holder: Item, index: Int?, indexValid: Boolean, propertyData: ArmorPropertyData) {
    Debugging.recomposition("ArmorPropertySelectText")

    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val selected = index != null && indexValid && holder.armorProperties?.get(index)?.id == propertyData.id

    val onItemClick: () -> Unit = {
        val properties = holder.armorProperties
            ?: throw RuntimeException("[ArmorPropertySelectText] non-null assertion failed: holder.armorProperties must not be null")

        val newProperty = ArmorProperty(propertyData.id, holder)

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
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary), onClick = onItemClick)
            .hoverable(interaction)
            .drawBehind {
                drawRect(
                    Color.White,
                    alpha = if (selected) 0.3f else if (hovered) 0.15f else 0f,
                    topLeft = Offset(-30.dp.value, 5.dp.value),
                    size = Size(size.width + 60.dp.value, size.height - 10.dp.value)
                )
            }
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = propertyData.description!!,
            style = TextStyle(fontSize = 26.sp, color = Color.White)
        )
        Text(
            text = propertyData.id,
            style = TextStyle(fontSize = 18.sp, color = Color.White)
        )
    }
}
