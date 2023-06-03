package composable.inventory

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import blackstone.states.arctic
import dungeons.DungeonsPower
import dungeons.IngameImages
import arctic.states.Item
import blackstone.states.items.*

@Composable
fun EquippedItems(items: List<Item?>) {
    Debugging.recomposition("EquippedItems")

    var collapsed by remember { mutableStateOf(false) }

    Row {
        EquipmentItemsToggleButton(collapsed = collapsed, onClick = { collapsed = !collapsed })
        EquippedItemsToggleAnimator(collapsed) {
            ItemsGrid(columns = if (it) 6 else 3, items = items) { index, item ->
                ItemView(item, it)
            }
        }
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
    Debugging.recomposition("EquipmentItemsToggleButton")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    val image = remember { IngameImages.get { "/Game/UI/Materials/Menu/arrow_gamemode.png" } }

    val rotation by animateFloatAsState(if (collapsed) 180f else 0f)

    Box (
        modifier = Modifier
            .size(60.dp)
            .offset(y = 10.dp)
            .clickable(source, null, onClick = onClick)
            .hoverable(source)
            .padding(15.dp)
            .rotate(rotation)
            .drawBehind {
                drawImage(image, alpha = if (collapsed) 1.0f else if (hovered) 0.5f else 0.3f, dstSize = IntSize((image.width * 0.75f * 1.dp.value).toInt(), (image.height * 0.75f * 1.dp.value).toInt()))
            }
    )
}

@Composable
fun InventoryItems(items: List<Item>) {
    Debugging.recomposition("InventoryItems")

    var variantFilter by remember { mutableStateOf<String?>(null) }
    var rarityFilter by remember { mutableStateOf<String?>(null) }

    val filteredItems by remember(items) {
        derivedStateOf {
            items.filter {
                val variantMatched = (variantFilter == null || (if (variantFilter == "Enchanted") it.totalInvestedEnchantmentPoints > 0 else it.data.variant == variantFilter))
                val rarityMatched = (rarityFilter == null || it.rarity == rarityFilter)

                variantMatched && rarityMatched
            }
        }
    }

    Row {
        ItemsFilterer(
            variantFilter = variantFilter,
            rarityFilter = rarityFilter,
            setVariantFilter = { variantFilter = if (variantFilter == it) null else it },
            setRarityFilter = { rarityFilter = if (rarityFilter == it) null else it }
        )
        ItemsGrid(items = filteredItems) { _, item ->
            ItemView(item)
        }
    }
}

@Composable
fun ItemsFilterer(variantFilter: String?, rarityFilter: String?, setVariantFilter: (String?) -> Unit, setRarityFilter: (String?) -> Unit) {
    Debugging.recomposition("ItemsFilterer")

    val filterableVariants = remember { listOf("Melee", "Armor", "Ranged", "Artifact", "Enchanted") }
    val filterableRarities = remember { listOf("Unique", "Rare", "Common") }

    Column(modifier = Modifier.padding(top = 15.dp), horizontalAlignment = Alignment.End) {
        for (type in filterableVariants) {
            ItemVariantFilter(variant = type, selected = type == variantFilter) { setVariantFilter(type) }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        for (rarity in filterableRarities) {
            ItemRarityFilter(rarity = rarity, selected = rarity == rarityFilter) { setRarityFilter(rarity) }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        ItemAddButton()
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemAddButton() {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .size(60.dp)
            .hoverable(source)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) { if(!arctic.alerts.checkAvailable()) arctic.creation.enable() }
            .padding(10.dp)
            .drawBehind {
                drawRect(Color(if(hovered) 0xffffffff else 0xff79706b), topLeft = Offset(size.width / 2 - 2.dp.value, 8.dp.value), size = Size(4.dp.value, size.height - 16.dp.value))
                drawRect(Color(if(hovered) 0xffffffff else 0xff79706b), topLeft = Offset(8.dp.value, size.height / 2 - 2.dp.value), size = Size(size.width - 16.dp.value, 4.dp.value))
            }
    )
}

@Composable
fun ItemVariantFilter(variant: String, selected: Boolean, onClick: () -> Unit) {
    Debugging.recomposition("ItemVariantFilter")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val image = remember(variant, selected, hovered) { VariantFilterIcon(variant, selected || hovered) }
    val alpha = if (selected) 1f else if (hovered) 0.65f else 1f
    Image(image, "filter_type_frame", modifier = Modifier.size(60.dp).hoverable(source).clickable(source, null, onClick = onClick).padding(10.dp).alpha(alpha))
}

@Composable
fun ItemRarityFilter(rarity: String, selected: Boolean, onClick: () -> Unit) {
    Debugging.recomposition("ItemRarityFilter")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val alpha = if (selected) 1f else if (hovered) 0.55f else 0.35f

    Box(
        modifier = Modifier
            .size(60.dp)
            .hoverable(source)
            .clickable(source, null, onClick = onClick)
            .rotate(-20f)
            .drawBehind {
                drawRoundRect(
                    RarityColor(rarity, RarityColorType.Opaque).copy(alpha = alpha),
                    topLeft = Offset(10.dp.value, size.height / 2 - 6.dp.value),
                    size = Size(size.width - 20.dp.value, 12.dp.value),
                    cornerRadius = CornerRadius(3.dp.value)
                )
            }
            .rotate(20f)
    )
}

@Composable
fun Divider() {
    Spacer(modifier = Modifier.height(5.dp))
    Box(modifier = Modifier.fillMaxWidth().height(2.dp).padding(horizontal = 10.dp).padding(start = 60.dp).background(Color(0xff666666)))
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
fun <T> ItemView(item: T, simplified: Boolean = false) where T: Item? =
    ItemViewInteractable(item, item != null) {
        if (item == null) DummyItemIcon()
        else ItemIcon(item, simplified)
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemViewInteractable(item: Item?, enabled: Boolean = true, content: @Composable BoxScope.() -> Unit) {
    Debugging.recomposition("ItemViewInteractable")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .aspectRatio(1f / 1f)
            .padding(5.dp)
            .hoverable(source)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary), enabled = enabled) { if (item != null) arctic.selection.select(item, 0) }
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Secondary), enabled = enabled) { if (item != null) arctic.selection.select(item, 1) }
            .drawBehind {
                val brush =
                    if (arctic.selection.selected(item))
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
fun DummyItemIcon() =
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(RarityColor("Common", RarityColorType.Translucent), Color.Transparent)))
            .border(7.dp, Brush.linearGradient(listOf(RarityColor("Common", RarityColorType.Opaque), Color.Transparent, RarityColor("Common", RarityColorType.Opaque))), shape = RectangleShape)
            .padding(20.dp)
    )

@Composable
fun BoxScope.ItemIcon(item: Item, simplified: Boolean) {
    Debugging.recomposition("ItemIcon")

    val rarity = item.rarity
    val power = DungeonsPower.toInGamePower(item.power).toInt()
    val totalEnchantmentPoints = item.totalInvestedEnchantmentPoints

    Image(
        bitmap = item.data.inventoryIcon,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawRect(RarityBackgroundGradient(rarity))
                if (item.netheriteEnchant != null) drawRect(GlidedItemBackgroundGradient())

                drawContent()

                drawRect(PowerBackgroundGradient())
                if (totalEnchantmentPoints > 0) drawRect(EnchantmentPointsBackgroundGradient())

                drawRect(RarityBorderGradient1(rarity), style = Stroke(5.dp.value))
                drawRect(RarityBorderGradient2(rarity, size.width, size.height), style = Stroke(5.dp.value))
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

    if (totalEnchantmentPoints != 0) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.TopEnd).padding(vertical = 8.dp, horizontal = 13.dp)
        ) {
            Image(
                bitmap = IngameImages.get { "/Game/UI/Materials/Inventory2/Item/salvage_enchanticon.png" },
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

    if (item.markedNew == true) {
        Image(
            bitmap = IngameImages.get { "/Game/UI/Materials/HotBar2/Icons/inventoryslot_newitem.png" },
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopStart).fillMaxSize(0.2f).offset(2.dp, (-1.5).dp).scale(scaleX = -1f, scaleY = 1f)
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
private fun GlidedItemBackgroundGradient() =
    Brush.linearGradient(0f to Color.Transparent, 0.5f to Color.Transparent, 1f to Color(0xaaffc847))

@Stable
private fun RarityBackgroundGradient(rarity: String) = Brush.linearGradient(listOf(RarityColor(rarity, RarityColorType.Translucent), Color.Transparent))

@Stable
private fun RarityBorderGradient1(rarity: String) =
    Brush.linearGradient(listOf(RarityColor(rarity, RarityColorType.Opaque), Color.Transparent, RarityColor(rarity, RarityColorType.Opaque).copy(alpha = 0.75f)))

@Stable
private fun RarityBorderGradient2(rarity: String, width: Float, height: Float) =
    Brush.linearGradient(listOf(RarityColor(rarity, RarityColorType.Opaque).copy(alpha = 0.5f), Color.Transparent), start = Offset(width, 0f), end = Offset(0f, height))
