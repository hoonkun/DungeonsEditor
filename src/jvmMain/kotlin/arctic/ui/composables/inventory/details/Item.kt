package arctic.ui.composables.inventory.details

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import arctic.states.Arctic
import arctic.states.EditorState
import arctic.states.ItemEnchantmentOverlayState
import arctic.ui.composables.atomic.*
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.DungeonsPower
import dungeons.IngameImages
import dungeons.Localizations
import dungeons.states.Enchantment
import dungeons.states.Item
import dungeons.states.extensions.addItem
import extensions.toFixed

@Composable
fun ItemDetail(item: Item?, editor: EditorState) {
    RootAnimator(item) {
        if (it != null) Content(it, editor)
        else Box(modifier = Modifier.fillMaxWidth().scale(1f / 1.3f))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RootAnimator(targetState: Item?, content: @Composable AnimatedVisibilityScope.(Item?) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter with exit using SizeTransform(clip = false)
        },
        contentAlignment = Alignment.Center,
        modifier = Modifier.scale(1.3f),
        content = content
    )

@Composable
private fun Content(item: Item, editor: EditorState) {
    Box(modifier = Modifier.wrapContentHeight().fillMaxWidth().scale(1f / 1.3f)) {
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
        Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
            Row { ItemAlterLeft(item) }

            ItemName(item.data.name ?: Localizations.UiText("unknown_item"))

            ItemDescription(item.data.flavour)
            Spacer(modifier = Modifier.height(20.dp))
            ItemDescription(item.data.description)

            val armorProperties = item.armorProperties
            if (armorProperties != null) {
                ItemArmorProperties(item, armorProperties)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                PowerEditField(
                    value = DungeonsPower.toInGamePower(item.power).toFixed(3).toString(),
                    onValueChange = {
                        if (it.toDoubleOrNull() != null) item.power = DungeonsPower.toSerializedPower(it.toDouble())
                    },
                    inputModifier = Modifier.requiredWidth(100.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                ItemAlterRight(item, editor)
            }

            val enchantments = item.enchantments
            if (enchantments != null) {
                Spacer(modifier = Modifier.height(30.dp))
                ItemEnchantments(enchantments)
            }
        }
    }
}

@Composable
private fun ItemAlterLeft(item: Item) {
    ItemRarityButton(item.data, item.rarity) { item.rarity = it }
    if (item.data.variant != "Artifact") {
        Spacer(modifier = Modifier.width(7.dp))
        ItemNetheriteEnchantButton(item)
        Spacer(modifier = Modifier.width(7.dp))
        ItemModifiedButton(item)
    }
}

@Composable
private fun TooltipText(text: String) =
    Text(
        text = Localizations.UiText(text),
        color = Color.White,
        fontSize = 20.sp,
        modifier = Modifier
            .background(Color(0xff191919), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemAlterRight(item: Item, editor: EditorState) {
    val transferText =
        if (!item.parent.equippedItems.contains(item) && item.where == editor.view) "transfer"
        else if (!item.parent.equippedItems.contains(item)) "pull"
        else null

    if (transferText != null) {
        val tooltipArgs =
            if (transferText == "transfer") arrayOf(item.where!!.other().localizedName)
            else arrayOf(item.where!!.localizedName, editor.view.localizedName)

        AnimatedTooltipArea(
            tooltip = { TooltipText(Localizations.UiText("${transferText}_tooltip", *tooltipArgs)) },
            delayMillis = 0
        ) {
            ItemAlterButton(Localizations.UiText(transferText, item.where!!.other().localizedName)) { item.transfer(editor) }
        }
    }

    Spacer(modifier = Modifier.width(7.dp))
    AnimatedTooltipArea(
        tooltip = { TooltipText(Localizations.UiText("change_type_tooltip")) },
        delayMillis = 0,
    ) {
        ItemAlterButton(Localizations.UiText("change_type")) { Arctic.overlayState.itemEdition = item }
    }
    Spacer(modifier = Modifier.width(7.dp))
    ItemAlterButton(Localizations.UiText("duplicate")) {
        if (item.where == editor.view) {
            if (editor.noSpaceInInventory)
                Arctic.overlayState.inventoryFull = true
            else
                item.parent.addItem(editor, item.copy(), item)
        } else {
            Arctic.overlayState.itemDuplication = item
        }
    }
    Spacer(modifier = Modifier.width(7.dp))
    ItemAlterButton(Localizations.UiText("delete"), color = Color(0x25ff6d0c)) { Arctic.overlayState.itemDeletion = item }
}

@Composable
private fun ItemNetheriteEnchantButton(holder: Item) {
    val enchantment = holder.netheriteEnchant

    val onClick = {
        val target = enchantment
            ?: Enchantment(holder, "Unset", isNetheriteEnchant = true).also { holder.netheriteEnchant = it }
        Arctic.overlayState.enchantment = ItemEnchantmentOverlayState(holder, target)
    }

    ItemAlterButton(
        color = Color(if (enchantment == null || enchantment.id == "Unset") 0x15ffffff else 0x40ffc847),
        horizontalPadding = if (enchantment == null || enchantment.id == "Unset") 4.dp else 10.dp,
        onClick = onClick
    ) {
        if (enchantment == null || enchantment.id == "Unset") {
            Image(
                bitmap = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png" },
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Image(
                bitmap = enchantment.data.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = (-2).dp)
                    .drawBehind {
                        drawImage(
                            image = IngameImages.get { "/Game/Content_DLC4/UI/Materials/Inventory/enchantSpecialUnique_Bullit.png" },
                            dstSize = IntSize(size.width.toInt(), size.height.toInt())
                        )
                    }
                    .scale(1.2f)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = Localizations["AncientLabels/iteminspector_gilded"]!!, fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
private fun ItemModifiedButton(holder: Item) {
    val modified = holder.modified == true

    ItemAlterButton(
        color = if (modified) Color(0x556f52ff) else Color(0x15ffffff),
        onClick = { holder.modified = !modified }
    ) {
        Text(text = if (modified) Localizations.UiText("modified") else "_", fontSize = 18.sp, color = Color.White)
        if (!modified) return@ItemAlterButton
        ItemTimesModifiedField("${holder.timesModified ?: 0}") { newValue ->
            if (newValue.toIntOrNull() != null)
                holder.timesModified = newValue.toInt().takeIf { it != 0 }
        }
        Text(text = Localizations.UiText("times"), fontSize = 18.sp, color = Color.White)
    }
}

@Composable
fun ItemTimesModifiedField(value: String, onValueChange: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val lineColor by animateColorAsState(
        targetValue = if (!focused) Color(0x00b2a4ff) else Color(0xffb2a4ff),
        animationSpec = tween(durationMillis = 250)
    )

    BasicTextField(
        value,
        onValueChange,
        textStyle = TextStyle(fontSize = 18.sp, color = Color.White, textAlign = TextAlign.End),
        singleLine = true,
        cursorBrush = SolidColor(Color.White),
        modifier = Modifier
            .onFocusChanged { focused = it.hasFocus }
            .requiredWidth(28.dp)
            .drawBehind {
                drawRect(lineColor, topLeft = Offset(0f, size.height), size = Size(size.width, densityDp(3)))
            }
    )
}

@Composable
private fun ItemName(text: String) =
    AutosizeText(
        text = text,
        maxFontSize = 60.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        style = TextStyle(shadow = Shadow(color = Color.Black, offset = Offset.Zero, blurRadius = 5f)),
        modifier = Modifier.offset(y = (-7).dp)
    )

@Composable
private fun ItemDescription(text: String?) {
    if (text == null) return

    Text(
        text = text,
        fontSize = 25.sp,
        color = Color.White,
        style = TextStyle(shadow = Shadow(color = Color.Black, offset = Offset.Zero, blurRadius = 5f))
    )
}

