package composable.inventory

import Localizations
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import editorState
import extensions.DungeonsPower
import extensions.GameResources
import states.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ColumnScope.AnimatedItemDetailView(targetState: Item?, type: String) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter with exit
        },
        contentAlignment = Alignment.Center
    ) {
        if (it == null) DummyItemView(type)
        else ItemDetailView(it)
    }

@Composable
private fun DummyItemView(type: String) =
    Box(modifier = Modifier.wrapContentHeight().fillMaxWidth().requiredHeightIn(min = 400.dp), contentAlignment = Alignment.Center) {
        ItemDescriptionText("${if (type == "primary") "왼쪽 클릭" else "오른쪽 클릭"}으로 비교할 대상을 추가해보세요!")
    }

@Composable
private fun ItemDetailView(item: Item) {
    val enchantmentSlots = item.enchantmentSlots
    val netheriteEnchant = item.netheriteEnchant

    ItemDetailViewRoot {
        ItemImage(item = item)
        ItemDataColumn {
            Row {
                RarityIndicator(item.rarity)
                Spacer(modifier = Modifier.width(10.dp))
                NetheriteEnchant(parentItem = item, enchantment = netheriteEnchant)
                Spacer(modifier = Modifier.width(10.dp))
                Modified(parentItem = item)
            }

            Row(modifier = Modifier.height(75.dp)) { ItemNameText(text = item.Name()) }

            ItemDescriptionText(text = item.Flavour())
            ItemDescriptionText(text = item.Description())

            Spacer(modifier = Modifier.height(20.dp))

            ArmorProperties(properties = item.armorProperties)

            PowerEditField(
                value = DungeonsPower.toInGamePower(item.power).toString(),
                onValueChange = {
                    if (it.toFloatOrNull() != null) item.power = DungeonsPower.toSerializedPower(it.toFloat())
                }
            )

            if (enchantmentSlots != null) ItemEnchantmentsView(enchantmentSlots)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modified(parentItem: Item) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    val modified = parentItem.modified == true

    Row(
        modifier = Modifier
            .height(38.dp)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) {
                parentItem.modified = !modified
                if (modified) parentItem.timesModified = null
                else parentItem.timesModified = 1
            }
            .hoverable(source)
            .background(if (modified) Color(0x556f52ff) else Color(0x15ffffff), shape = RoundedCornerShape(6.dp))
            .drawBehind { drawInteractionBorder(hovered, false) }
            .padding(vertical = 4.dp, horizontal = 10.dp)
    ) {
        Text(text = if (modified) "효과 변경" else "_", fontSize = 20.sp, color = Color.White)
        if (modified) {
            UnlabeledField("${parentItem.timesModified}") { if (it.toIntOrNull() != null) parentItem.timesModified = it.toInt() }
            Text(text = "번", fontSize = 20.sp, color = Color.White)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NetheriteEnchant(parentItem: Item, enchantment: Enchantment?) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val selected = enchantment != null && editorState.detailState.selectedEnchantment == enchantment

    if (enchantment == null || enchantment.id == "Unset") {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) {
                    var netheriteEnchant = parentItem.netheriteEnchant
                    if (netheriteEnchant == null) {
                        netheriteEnchant = Enchantment(parentItem, "Unset", 0, 0)
                        parentItem.netheriteEnchant = netheriteEnchant
                    }
                    editorState.detailState.selectEnchantment(netheriteEnchant)
                }
                .hoverable(source)
                .background(Color(0x15ffffff), shape = RoundedCornerShape(6.dp))
                .drawBehind { drawInteractionBorder(hovered, selected) }
                .padding(4.dp)
        ) {
            Image(
                bitmap = GameResources.image { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png" },
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentSize()
                .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) { editorState.detailState.selectEnchantment(enchantment) }
                .hoverable(source)
                .background(Color(0x40ffc847), RoundedCornerShape(6.dp))
                .drawBehind { drawInteractionBorder(hovered, selected) }
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            Box(modifier = Modifier.size(30.dp)) {
                Image(
                    bitmap = GameResources.image { "/Game/Content_DLC4/UI/Materials/Inventory/enchantSpecialUnique_Bullit.png" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxHeight().aspectRatio(1f / 1f)
                )
                Image(
                    bitmap = enchantment.Image(),
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
        item.LargeIcon(),
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
        bitmap = GameResources.image { "/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png" },
        contentDescription = null,
        modifier = Modifier.size(30.dp)
    )

@Composable
private fun PowerEditField(value: String, onValueChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PowerIcon()
        Spacer(modifier = Modifier.width(10.dp))
        LabeledInput(label = Localizations["/gearpower_POWER"]!!, value = value, onValueChange = onValueChange)
    }
}

@Composable
private fun LabeledInput(label: String, value: String, onValueChange: (String) -> Unit) {
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
private fun ArmorProperties(properties: List<ArmorProperty>?) {
    if (properties == null) return

    val groupedProperties by remember {
        derivedStateOf {
            val sorted = properties.sortedBy { it.Description()?.length }
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


    for (propertyRow in groupedProperties) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            for (property in propertyRow) {
                val text = property.Description()!!
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Image(
                        GameResources.image { "/Game/UI/Materials/Inventory2/Inspector/${property.IconName()}_bullit.png" },
                        null,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    ItemDescriptionText(text = text)
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}

fun groupByLength(input: List<ArmorProperty>): List<List<ArmorProperty>> {
    val result = mutableListOf<MutableList<ArmorProperty>>(mutableListOf())
    input.forEach {
        val description = it.Description() ?: return@forEach
        val long = description.length > 15
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

@Composable
fun RarityIndicator(rarity: Item.Rarity) {
    Text(
        text = Localizations["/rarity_${rarity.name.lowercase()}"]!!,
        fontSize = 20.sp,
        color = Color.White,
        modifier = Modifier
            .height(38.dp)
            .background(rarity.TranslucentColor(), shape = RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp, horizontal = 10.dp)
    )
}
