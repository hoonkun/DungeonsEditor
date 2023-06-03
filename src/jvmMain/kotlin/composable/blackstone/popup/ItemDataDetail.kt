package composable.blackstone.popup

import dungeons.ItemData
import dungeons.Localizations
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import blackstone.states.*
import blackstone.states.items.RarityColor
import blackstone.states.items.RarityColorType
import blackstone.states.items.addItem
import composable.inventory.PowerEditField
import composable.inventory.drawInteractionBorder
import dungeons.DungeonsPower
import dungeons.IngameImages
import extensions.toFixed

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemDataDetail(itemData: ItemData) {

    var rarity by remember { mutableStateOf(if (itemData.unique) "Unique" else "Common") }
    var power by remember { mutableStateOf(DungeonsPower.toSerializedPower(DungeonsPower.playerPower(arctic.requireStored).toDouble())) }

    val name = itemData.name
    val description = itemData.description
    val flavour = itemData.flavour

    Box (
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(385.dp)
            .background(Color(0xff080808))
            .onClick {  }
            .padding(40.dp)
    ) {
        Box (contentAlignment = Alignment.TopEnd, modifier = Modifier.requiredWidth(950.dp)) {
            Row {
                Image(itemData.largeIcon, null, modifier = Modifier.fillMaxHeight().aspectRatio(1f / 1f))
                Spacer(modifier = Modifier.width(25.dp))
                Column {
                    AlterButton(
                        text = "${if (itemData.limited) "시즌한정 " else ""}${Localizations["/rarity_${rarity.lowercase()}"]}",
                        enabled = !itemData.unique,
                        color = RarityColor(rarity, RarityColorType.Translucent)
                    ) { rarity = if (rarity == "Common") "Rare" else "Common" }
                    Text(
                        text = name ?: "알 수 없는 아이템",
                        color = Color.White,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (description != null) Text(text = description, color = Color.White, fontSize = 25.sp)
                    if (flavour != null) Text(text = flavour, color = Color.White, fontSize = 25.sp)

                    Spacer(modifier = Modifier.weight(1f))

                    PowerEditField(
                        value = DungeonsPower.toInGamePower(power).toFixed(4).toString(),
                        onValueChange = {
                            if (it.toDoubleOrNull() != null) power = DungeonsPower.toSerializedPower(it.toDouble())
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.offset(y = 365.dp)) {
                if (itemData.variant != "Artifact") WarningText(text = "다른 옵션들은 추가한 뒤에 우측 영역에서 수정할 수 있어요!")
                if (itemData.variant == "Armor") WarningText(text = "추가 후 표시되는 기본 ArmorProperty 값은 수기로 기록된 것으로, 정확하지 않을 수 있습니다.")
            }
        }
        AddButton {
            val newItem = Item(
                parent = arctic.requireStored,
                inventoryIndex = 0,
                power = power,
                rarity = rarity,
                type = itemData.type,
                upgraded = false,
                enchantments = if (itemData.variant != "Artifact") listOf() else null,
                armorProperties = if (itemData.variant == "Armor") itemData.builtInProperties.map { ArmorProperty(id = it.id, rarity = "Common") } else null,
                markedNew = true
            )
            newItem.parent.addItem(newItem)
            arctic.creation.disable()
            arctic.creation.target = null
        }
    }

}

@Composable
fun WarningText(text: String) {
    Text(text = text, color = Color.White.copy(alpha = 0.45f), fontSize = 18.sp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlterButton(text: String, enabled: Boolean = true, color: Color, onClick: () -> Unit) {
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
fun BoxScope.AddButton(onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    val hoverAlpha by animateFloatAsState(if (hovered) 0.8f else 0f)
    val baseAlpha by animateFloatAsState(if (hovered) 0.8f else 0.7f)

    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(x = (-100).dp)
            .hoverable(source)
            .onClick(onClick = onClick)
            .graphicsLayer { clip = false }
    ) {
        Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            AddButtonIcon(modifier = Modifier.blur(10.dp).alpha(hoverAlpha))
            AddButtonIcon(modifier = Modifier.alpha(baseAlpha))
        }
        Text(
            text = "추가",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 25.dp)
                .alpha(baseAlpha)
        )
    }
}

@Composable
fun AddButtonIcon(modifier: Modifier = Modifier) {
    Image(
        IngameImages.get { "/Game/UI/Materials/Character/right_arrow_carousel.png" },
        null,
        filterQuality = FilterQuality.None,
        modifier = modifier.then(Modifier.size(125.dp).padding(12.5.dp))
    )
}

