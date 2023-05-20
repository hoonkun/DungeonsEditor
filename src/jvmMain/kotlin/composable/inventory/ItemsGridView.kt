package composable.inventory

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import extensions.DungeonsPower
import extensions.GameResources
import states.EditorState
import states.InventoryEditorState
import states.Item
import states.Items

@Composable
fun EquippedItems(items: Items, editorState: EditorState) {
    var collapsed by remember { mutableStateOf(false) }

    Box {
        EquippedItemsToggleAnimator(collapsed) {
            ItemsGrid(columns = if (it) 6 else 3, items = items.equipped) { index, item ->
                ItemView(item, editorState, it, -index - 1)
            }
        }
        EquipmentItemsToggleButton(collapsed = collapsed, onClick = { collapsed = !collapsed })
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun EquippedItemsToggleAnimator(targetState: Boolean, content: @Composable AnimatedVisibilityScope.(it: Boolean) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { -it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter with exit
        },
        content = content
    )

@Composable
private fun EquipmentItemsToggleButton(collapsed: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }

    Box (
        modifier = Modifier
            .size(60.dp)
            .offset((-70).dp)
            .clickable(source, null, onClick = onClick)
            .drawBehind {
                drawRect(
                    if (collapsed) Color.White else Color(0xff79706b),
                    topLeft = Offset(12f, this.size.height / 2 - 5f),
                    size = Size(this.size.width - 24f, 7f)
                )
            }
    )
}

@Composable
fun InventoryItems(items: Items, editorState: EditorState) {
    var typeFilter by remember { mutableStateOf<Item.ItemType?>(null) }
    var rarityFilter by remember { mutableStateOf<Item.Rarity?>(null) }

    val filteredItems by remember { derivedStateOf { items.filter(typeFilter, rarityFilter) } }

    Box {
        ItemsFilterer(
            typeFilter = typeFilter,
            rarityFilter = rarityFilter,
            setTypeFilter = { typeFilter = if (typeFilter == it) null else it },
            setRarityFilter = { rarityFilter = if (rarityFilter == it) null else it }
        )
        ItemsGrid(items = filteredItems) { _, item ->
            ItemView(item, editorState, index = item.inventoryIndex ?: 0)
        }
    }
}

@Composable
fun ItemsFilterer(typeFilter: Item.ItemType?, rarityFilter: Item.Rarity?, setTypeFilter: (Item.ItemType?) -> Unit, setRarityFilter: (Item.Rarity?) -> Unit) {
    val filterableTypes = remember { Item.ItemType.values().filter { it != Item.ItemType.Unknown } }
    val filterableRarities = remember { Item.Rarity.values() }

    Column(modifier = Modifier.offset(x = (-70).dp).padding(top = 15.dp), horizontalAlignment = Alignment.End) {
        for (type in filterableTypes) {
            ItemTypeFilter(type = type, selected = type == typeFilter) { setTypeFilter(type) }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        for (rarity in filterableRarities) {
            ItemRarityFilter(rarity = rarity, currentType = typeFilter, selected = rarity == rarityFilter) { setRarityFilter(rarity) }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ItemTypeFilter(type: Item.ItemType, selected: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val image = remember(type, selected) {
        GameResources.image { "/Game/UI/Materials/Inventory2/Filter/${type.FilterIconName(selected)}.png" }
    }
    Image(image, "filter_type_frame", modifier = Modifier.size(60.dp).clickable(source, null, onClick = onClick).padding(10.dp))
}

@Composable
fun ItemRarityFilter(rarity: Item.Rarity, currentType: Item.ItemType?, selected: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val alpha = if (selected) 1f else 0.35f

    val frameImage = remember(rarity) {
        GameResources.image { "/Game/UI/Materials/Notification/${rarity.FrameName()}" }
    }
    val overlayImage = remember(currentType) {
        GameResources.image { "/Game/UI/Materials/MissionSelectMap/inspector/loot/${currentType?.DropIconName() ?: "drop_unknown.png"}" }
    }
    Box(modifier = Modifier.size(60.dp).clickable(source, null, onClick = onClick), contentAlignment = Alignment.Center) {
        Image(frameImage, "filter_rarity_frame", alpha = alpha, modifier = Modifier.fillMaxSize())
        Image(overlayImage, "filter_rarity_overlay", alpha = alpha, modifier = Modifier.fillMaxSize(0.5f))
    }
}

@Composable
fun Divider() {
    Spacer(modifier = Modifier.height(5.dp))
    Box(modifier = Modifier.fillMaxWidth().height(2.dp).padding(horizontal = 10.dp).background(Color(0xff666666)))
    Spacer(modifier = Modifier.height(5.dp))
}

@Composable
fun <T>ItemsGrid(columns: Int = 3, items: List<T>, content: @Composable LazyGridItemScope.(Int, T) -> Unit) where T: Item? =
    LazyVerticalGrid(
        GridCells.Fixed(columns),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) { itemsIndexed(items, itemContent = content) }

@Composable
fun <T> ItemView(item: T, editorState: EditorState, simplified: Boolean = false, index: Int) where T: Item? {

    ItemViewInteractable(editorState.inventoryState, index) {
        if (item == null) DummyItemIcon()
        else ItemIcon(item, simplified)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemViewInteractable(state: InventoryEditorState, index: Int, content: @Composable BoxScope.() -> Unit) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .aspectRatio(1f / 1f)
            .padding(5.dp)
            .hoverable(source)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) { state.select(index, "primary") }
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Secondary)) { state.select(index, "secondary") }
            .drawBehind {
                val brush =
                    if (state.selectedIndexes.contains(index))
                        Brush.linearGradient(listOf(Color(0xeeffffff), Color(0xaaffffff), Color(0xeeffffff)))
                    else if (hovered)
                        Brush.linearGradient(listOf(Color(0x75ffffff), Color(0x25ffffff), Color(0x75ffffff)))
                    else
                        return@drawBehind

                drawRect(
                    brush = brush,
                    topLeft = Offset(-10.dp.value, -10.dp.value),
                    size = Size(size.width + 20.dp.value, size.height + 20.dp.value),
                    style = Stroke(width = 4.dp.value)
                )
            },
        content = content
    )
}

@Composable
fun DummyItemIcon() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Item.Rarity.Common.TranslucentColor(), Color.Transparent)))
            .border(7.dp, Brush.linearGradient(listOf(Item.Rarity.Common.OpaqueColor(), Color.Transparent, Item.Rarity.Common.OpaqueColor())), shape = RectangleShape)
            .padding(20.dp)
    )
}

@Composable
fun BoxScope.ItemIcon(item: Item, simplified: Boolean) {
    val rarity = item.rarity
    val power = DungeonsPower.toInGamePower(item.power).toInt()
    val totalEnchantmentPoints = item.TotalInvestedEnchantmentPoints()

    Image(
        bitmap = item.InventoryIcon(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawRect(rarity.BackgroundGradient())

                drawContent()

                drawRect(PowerBackgroundGradient())
                if (totalEnchantmentPoints > 0) drawRect(EnchantmentPointsBackgroundGradient())

                drawRect(rarity.BorderGradient1(), style = Stroke(5.dp.value))
                drawRect(rarity.BorderGradient2(size.width, size.height), style = Stroke(5.dp.value))
            }
            .padding(if (simplified) 12.5.dp else 20.dp)
    )

    if (simplified) return

    Text(
        text = "$power",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        modifier = Modifier.align(Alignment.BottomEnd).padding(vertical = 8.dp, horizontal = 13.dp)
    )

    if (totalEnchantmentPoints == 0) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.align(Alignment.TopEnd).padding(vertical = 8.dp, horizontal = 13.dp)
    ) {
        Image(
            bitmap = GameResources.image { "/Game/UI/Materials/Inventory2/Item/salvage_enchanticon.png" },
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$totalEnchantmentPoints",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            modifier = Modifier.offset(y = (-2).dp)
        )
    }

}

@Stable
private fun PowerBackgroundGradient() =
    Brush.linearGradient(0f to Color.Transparent, 0.5f to Color.Transparent, 1f to Color(0x70000000))

@Stable
private fun DrawScope.EnchantmentPointsBackgroundGradient() =
    Brush.linearGradient(
        0f to Color.Transparent, 0.6f to Color.Transparent, 1f to Color(0x60b442f6),
        start = Offset(0f, this.size.height),
        end = Offset(this.size.width, 0f)
    )

@Stable
private fun Item.Rarity.BackgroundGradient() = Brush.linearGradient(listOf(TranslucentColor(), Color.Transparent))

@Stable
private fun Item.Rarity.BorderGradient1() =
    Brush.linearGradient(listOf(OpaqueColor(), Color.Transparent, OpaqueColor().copy(alpha = 0.75f)))

@Stable
private fun Item.Rarity.BorderGradient2(width: Float, height: Float) =
    Brush.linearGradient(listOf(OpaqueColor().copy(alpha = 0.5f), Color.Transparent), start = Offset(width, 0f), end = Offset(0f, height))
