package composable.inventory

import dungeons.Localizations
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import blackstone.states.arctic
import arctic.states.ArmorProperty
import arctic.states.Enchantment
import arctic.states.Item
import blackstone.states.items.*
import dungeons.DungeonsPower
import dungeons.IngameImages
import extensions.toFixed

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ColumnScope.AnimatedItemDetailView(targetState: Item?) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter with exit using SizeTransform(clip = false)
        },
        contentAlignment = Alignment.Center
    ) {
        if (it == null) DummyItemView()
        else ItemDetailView(it)
    }

@Composable
private fun DummyItemView() =
    Box(modifier = Modifier.wrapContentHeight().fillMaxWidth())

@Composable
private fun ItemDetailView(item: Item) {
    Debugging.recomposition("ItemDetailView")

    val enchantments = item.enchantments
    val netheriteEnchant = item.netheriteEnchant

    ItemDetailViewRoot {
        ItemImage(item = item)
        ItemDataColumn {
            Row {
                AlterButton(
                    text = "${if(item.data.limited) "시즌한정 " else ""}${Localizations["/rarity_${item.rarity.lowercase()}"]}",
                    color = RarityColor(item.rarity, RarityColorType.Translucent),
                    enabled = !item.data.unique
                ) { item.rarity = if (item.rarity == "Common") "Rare" else "Common" }
                if (item.data.variant != "Artifact") {
                    Spacer(modifier = Modifier.width(7.dp))
                    NetheriteEnchant(parentItem = item, enchantment = netheriteEnchant)
                    Spacer(modifier = Modifier.width(7.dp))
                    Modified(parentItem = item)
                }
                Spacer(modifier = Modifier.weight(1f))
                AlterButton("타입 변경") { arctic.edition.enable(item) }
                Spacer(modifier = Modifier.width(7.dp))
                AlterButton("복제") {
                    if (item.where() == arctic.view) {
                        if (!arctic.alerts.checkAvailable())
                            item.parent.addItem(item.copy(), item)
                    } else {
                        arctic.duplication.target = item
                    }
                }
                Spacer(modifier = Modifier.width(7.dp))
                AlterButton("삭제") { arctic.deletion.target = item }
            }

            Row(modifier = Modifier.height(75.dp)) { ItemNameText(text = item.data.name ?: "알 수 없는 아이템") }

            ItemDescriptionText(text = item.data.flavour)
            ItemDescriptionText(text = item.data.description)

            Spacer(modifier = Modifier.height(20.dp))

            ArmorProperties(item = item, properties = item.armorProperties)

            PowerEditField(
                value = DungeonsPower.toInGamePower(item.power).toFixed(4).toString(),
                onValueChange = {
                    if (it.toDoubleOrNull() != null) item.power = DungeonsPower.toSerializedPower(it.toDouble())
                }
            )

            if (enchantments != null) ItemEnchantmentsView(enchantments)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlterButton(text: String, color: Color = Color(0x15ffffff), enabled: Boolean = true, onClick: () -> Unit) {
    Debugging.recomposition("Modified")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .height(38.dp)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary), enabled = enabled, onClick = onClick)
            .hoverable(source, enabled)
            .background(color, shape = RoundedCornerShape(6.dp))
            .drawBehind { drawInteractionBorder(hovered, false) }
            .padding(vertical = 4.dp, horizontal = 10.dp)
    ) {
        Text(text = text, fontSize = 20.sp, color = Color.White)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modified(parentItem: Item) {
    Debugging.recomposition("Modified")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    val modified = parentItem.modified == true

    Row(
        modifier = Modifier
            .height(38.dp)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) { parentItem.modified = !modified }
            .hoverable(source)
            .background(if (modified) Color(0x556f52ff) else Color(0x15ffffff), shape = RoundedCornerShape(6.dp))
            .drawBehind { drawInteractionBorder(hovered, false) }
            .padding(vertical = 4.dp, horizontal = 10.dp)
    ) {
        Text(text = if (modified) "효과 변경" else "_", fontSize = 20.sp, color = Color.White)
        if (modified) {
            UnlabeledField("${parentItem.timesModified ?: 0}") {  newValue ->
                if (newValue.toIntOrNull() != null) {
                    parentItem.timesModified = newValue.toInt().takeIf { it != 0 }
                }
            }
            Text(text = "번", fontSize = 20.sp, color = Color.White)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NetheriteEnchant(parentItem: Item, enchantment: Enchantment?) {
    Debugging.recomposition("NetheriteEnchant")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val selected = enchantment != null && arctic.enchantments.isDetailTarget(enchantment)

    if (enchantment == null || enchantment.id == "Unset") {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) {
                    var netheriteEnchant = parentItem.netheriteEnchant
                    if (netheriteEnchant == null) {
                        netheriteEnchant = Enchantment("Unset", parentItem, isNetheriteEnchant = true)
                        parentItem.netheriteEnchant = netheriteEnchant
                    }
                    arctic.enchantments.viewDetail(netheriteEnchant)
                }
                .hoverable(source)
                .background(Color(0x15ffffff), shape = RoundedCornerShape(6.dp))
                .drawBehind { drawInteractionBorder(hovered, selected) }
                .padding(4.dp)
        ) {
            Image(
                bitmap = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png" },
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentSize()
                .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) { arctic.enchantments.viewDetail(enchantment) }
                .hoverable(source)
                .background(Color(0x40ffc847), RoundedCornerShape(6.dp))
                .drawBehind { drawInteractionBorder(hovered, selected) }
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            Box(modifier = Modifier.size(30.dp)) {
                Image(
                    bitmap = IngameImages.get { "/Game/Content_DLC4/UI/Materials/Inventory/enchantSpecialUnique_Bullit.png" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxHeight().aspectRatio(1f / 1f)
                )
                Image(
                    bitmap = enchantment.data.icon,
                    contentDescription = null,
                    modifier = Modifier.fillMaxHeight().aspectRatio(1f / 1f).scale(1.15f)
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "화려한", fontSize = 20.sp, color = Color.White)
        }
    }
}

@Stable
fun DrawScope.drawInteractionBorder(hovered: Boolean, selected: Boolean) {
    if (!hovered && !selected) return
    drawRoundRect(
        brush = SolidColor(if (selected) Color.White else Color.White.copy(0.35f)),
        cornerRadius = CornerRadius(6.dp.value, 6.dp.value),
        style = Stroke(3.dp.value)
    )
}

@Composable
private fun ItemDetailViewRoot(content: @Composable BoxScope.() -> Unit) =
    Box(modifier = Modifier.wrapContentHeight().fillMaxWidth(), content = content)

@Composable
private fun ItemDataColumn(content: @Composable ColumnScope.() -> Unit) =
    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), content = content)

@Composable
private fun ItemNameText(text: String) =
    Text(
        text = text,
        style = TextStyle(
            fontSize = 60.sp,
            shadow = Shadow(color = Color.Black, offset = Offset.Zero, blurRadius = 5f),
            fontWeight = FontWeight.Bold,
            color = Color.White
        ),
        modifier = Modifier.offset(y = (-7).dp)
    )

@Composable
private fun ItemDescriptionText(text: String?) {
    Debugging.recomposition("ItemDescription")

    if (text == null) return

    Text(
        text = text,
        style = TextStyle(
            fontSize = 25.sp,
            shadow = Shadow(color = Color.Black, offset = Offset.Zero, blurRadius = 5f),
            color = Color.White
        )
    )
}

@Composable
private fun BoxScope.ItemImage(item: Item) =
    Image(
        item.data.largeIcon,
        null,
        alpha = 0.25f,
        modifier = Modifier
            .fillMaxWidth(0.65f)
            .aspectRatio(1f / 1f)
            .align(Alignment.TopEnd)
            .offset((-10).dp, 60.dp)
    )

@Composable
private fun PowerIcon() =
    Image(
        bitmap = IngameImages.get { "/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png" },
        contentDescription = null,
        modifier = Modifier.size(30.dp)
    )

@Composable
fun PowerEditField(value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    Debugging.recomposition("PowerEditField")

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        PowerIcon()
        Spacer(modifier = Modifier.width(10.dp))
        LabeledInput(label = Localizations["/gearpower_POWER"]!!, value = value, onValueChange = onValueChange)
    }
}

@Composable
private fun LabeledInput(label: String, value: String, onValueChange: (String) -> Unit) {
    Debugging.recomposition("LabeledInput")

    var focused by remember { mutableStateOf(false) }
    val lineColor by animateColorAsState(if (!focused) Color(0xff888888) else Color(0xffff884c), animationSpec = tween(durationMillis = 250))

    Row {
        Text(label, fontSize = 25.sp, color = Color.White)
        Spacer(modifier = Modifier.width(15.dp))
        BasicTextField(
            value,
            onValueChange,
            textStyle = TextStyle(fontSize = 25.sp, color = Color.White),
            singleLine = true,
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier
                .onFocusChanged { focused = it.hasFocus }
                .drawBehind {
                    drawRect(lineColor, topLeft = Offset(0f, size.height), size = Size(size.width, 3.dp.value))
                }
        )
    }
}

@Composable
private fun ArmorProperties(item: Item, properties: List<ArmorProperty>?) {
    Debugging.recomposition("ArmorProperties")

    if (properties == null) return

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


    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            for (propertyRow in groupedProperties) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (property in propertyRow) {
                        ArmorPropertyView(property)
                        if (propertyRow.indexOf(property) == 0) Spacer(modifier = Modifier.width(20.dp))
                    }
                }
            }
        }
        ArmorPropertyButton(item = item, mode = if (arctic.armorProperties.hasDetailTarget) "Delete" else "Add")
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArmorPropertyButton(item: Item, mode: String) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .size(35.dp)
            .hoverable(source)
            .drawBehind {
                if (hovered) drawRoundRect(Color.White, alpha = 0.15f, cornerRadius = CornerRadius(6.dp.value, 6.dp.value))
                if (mode == "Add") drawRect(Color.White, topLeft = Offset(size.width / 2 - 2f, 8f), size = Size(4f, size.height - 16f))
                drawRect(Color.White, topLeft = Offset(8f, size.height / 2 - 2f), size = Size(size.width - 16f, 4f))
            }
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) {
                if (mode == "Add") {
                    arctic.armorProperties.requestCreate(item)
                } else {
                    val selected = arctic.armorProperties.detailTarget ?: return@onClick
                    selected.holder.armorProperties?.remove(selected)
                    arctic.armorProperties.closeDetail()
                }
            }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.ArmorPropertyView(property: ArmorProperty) {
    Debugging.recomposition("ArmorPropertyView")

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val selected = arctic.armorProperties.isDetailTarget(property)

    Row(modifier = Modifier.weight(1f)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .hoverable(source)
                .drawBehind {
                    drawRoundRect(
                        SolidColor(Color.White),
                        alpha = if (selected) 0.2f else if (hovered) 0.1f else 0.0f,
                        cornerRadius = CornerRadius(6.dp.value, 6.dp.value),
                        topLeft = Offset(-10.dp.value, 0f),
                        size = Size(size.width + 20.dp.value, size.height)
                    )
                }
                .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) { arctic.armorProperties.viewDetail(property) }
        ) {
            Image(
                ArmorPropertyRarityIcon(property.rarity),
                null,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            ItemDescriptionText(text = property.data.description ?: property.id)
        }
    }
}

fun groupByLength(input: List<ArmorProperty>): List<List<ArmorProperty>> {
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

@Composable
fun UnlabeledField(value: String, onValueChange: (String) -> Unit) {
    Debugging.recomposition("UnlabeledField")

    var focused by remember { mutableStateOf(false) }
    val lineColor by animateColorAsState(
        if (!focused) Color(0x00b2a4ff) else Color(0xffb2a4ff),
        animationSpec = tween(durationMillis = 250)
    )

    BasicTextField(
        value,
        onValueChange,
        textStyle = TextStyle(fontSize = 20.sp, color = Color.White, textAlign = TextAlign.End),
        singleLine = true,
        cursorBrush = SolidColor(Color.White),
        modifier = Modifier
            .onFocusChanged { focused = it.hasFocus }
            .width(35.dp)
            .drawBehind {
                drawRect(lineColor, topLeft = Offset(0f, size.height), size = Size(size.width, 3.dp.value))
            }
    )
}
