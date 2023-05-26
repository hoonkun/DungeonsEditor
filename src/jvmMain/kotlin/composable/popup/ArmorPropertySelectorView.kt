package composable.popup

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blackstone.states.ArmorProperty

@Composable
fun ArmorPropertySelectorView(property: ArmorProperty) {
    Debugging.recomposition("ArmorPropertySelectorView")

    val sorted = remember { Database.current.armorProperties.filter { it.description != null }.sortedBy { it.description } }
    val lazy = rememberLazyListState(
        initialFirstVisibleItemIndex = sorted.indexOfFirst { it.id == property.id }.coerceAtLeast(0),
        initialFirstVisibleItemScrollOffset = -60.dp.value.toInt()
    )

    LazyColumn(modifier = Modifier.padding(30.dp), state = lazy) {
        items(sorted) { propertyData ->
            ArmorPropertySelectText(
                property,
                propertyData.description!!,
                propertyData
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArmorPropertySelectText(property: ArmorProperty, description: String, propertyData: ArmorPropertyData) {
    Debugging.recomposition("ArmorPropertySelectText")

    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) { property.id = propertyData.id }
            .hoverable(interaction)
            .drawBehind {
                drawRect(
                    Color.White,
                    alpha = if (property.id == propertyData.id) 0.3f else if (hovered) 0.15f else 0f,
                    topLeft = Offset(-30.dp.value, 5.dp.value),
                    size = Size(size.width + 60.dp.value, size.height - 10.dp.value)
                )
            }
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = description,
            style = TextStyle(fontSize = 26.sp, color = Color.White)
        )
        Text(
            text = propertyData.id,
            style = TextStyle(fontSize = 18.sp, color = Color.White)
        )
    }
}
