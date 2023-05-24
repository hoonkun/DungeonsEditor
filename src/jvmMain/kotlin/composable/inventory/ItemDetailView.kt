package composable.inventory

import Localizations
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
            Row(verticalAlignment = Alignment.Bottom) {
                ItemNameText(text = item.Name())
                Spacer(modifier = Modifier.width(20.dp))
                NetheriteEnchant(parentItem = item, enchantment = netheriteEnchant)
            }

            Spacer(modifier = Modifier.height(10.dp))

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
private fun NetheriteEnchant(parentItem: Item, enchantment: Enchantment?) {
    if (enchantment == null || enchantment.id == "Unset") {
        Box(
            modifier = Modifier
                .size(48.dp)
                .offset(y = (-8).dp)
                .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) {
                    val newEnchantment = Enchantment(parentItem, "Unset", 0, 1)
                    parentItem.netheriteEnchant = newEnchantment
                    editorState.detailState.selectEnchantment(newEnchantment)
                }
                .background(Color(0x15ffffff), shape = RoundedCornerShape(6.dp))
                .padding(4.dp)
        ) {
            Image(
                bitmap = GameResources.image { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png" },
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .offset(y = (-8).dp)
                .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) { editorState.detailState.selectEnchantment(enchantment) }
                .background(Color(0x40ffc847), RoundedCornerShape(6.dp))
                .padding(vertical = 4.dp, horizontal = 10.dp)
        ) {
            Box(modifier = Modifier.size(40.dp)) {
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
            Spacer(modifier = Modifier.width(10.dp))
            Text(fontSize = 26.sp, text = "화려한", color = Color.White, modifier = Modifier.offset(y = (-1).dp))
        }
    }
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
        )
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

    for (property in properties) {
        val text = property.Description()
        if (text != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(GameResources.image { "/Game/UI/Materials/Inventory2/Inspector/${property.IconName()}_bullit.png" }, null, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.width(10.dp))
                ItemDescriptionText(text = "$text")
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}
