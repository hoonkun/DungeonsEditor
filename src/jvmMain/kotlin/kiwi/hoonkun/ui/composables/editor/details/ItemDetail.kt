package kiwi.hoonkun.ui.composables.editor.details

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.*
import kiwi.hoonkun.ui.reusables.IfNotNull
import kiwi.hoonkun.ui.reusables.round
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.Enchantment
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import kiwi.hoonkun.utils.Retriever
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.values.DungeonsPower

@Composable
fun ItemDetail(item: Item?, editor: EditorState) {
    AnimatedContent(
        targetState = item,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter togetherWith exit using SizeTransform(clip = false)
        },
        contentAlignment = Alignment.Center
    ) {
        if (it != null) Content(item = it, editor = editor)
        else Spacer(modifier = Modifier.fillMaxWidth().scale(1f / 1.3f))
    }
}

@Composable
private fun Content(item: Item, editor: EditorState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawImage(
                    image = item.data.largeIcon,
                    dstOffset = Offset(size.width * 0.35f - 10f.dp.toPx(), 60f.dp.toPx()).round(),
                    dstSize = Size(size.width * 0.65f, size.width * 0.65f).round(),
                    alpha = 0.25f
                )
            }
            .padding(top = 20.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 10.dp)) {
            ItemRarityButton(item.data, item.rarity) { item.rarity = it }
            if (item.data.variant != "Artifact") {
                Spacer(modifier = Modifier.width(7.dp))
                ItemNetheriteEnchantButton(item)
                Spacer(modifier = Modifier.width(7.dp))
                ItemModifiedButton(item)
            }
        }

        ItemName(item.data.name ?: Localizations.UiText("unknown_item"))
        ItemDescription(item.data.flavour)
        ItemDescription(item.data.description)

        Spacer(modifier = Modifier.height(20.dp))

        IfNotNull(item.armorProperties) {
            ItemArmorProperties(item, it)
            Spacer(modifier = Modifier.height(20.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            PowerEditField(
                power = DungeonsPower.toInGamePower(item.power),
                onPowerChange = { item.power = DungeonsPower.toSerializedPower(it) },
                modifier = Modifier.requiredWidth(200.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            ItemAlterRight(item, editor)
        }

        IfNotNull(item.enchantments) {
            Spacer(modifier = Modifier.height(30.dp))
            ItemEnchantments(it)
        }
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

@Composable
private fun ItemAlterRight(item: Item, editor: EditorState) {
    val overlays = LocalOverlayState.current

    IfNotNull(item.where) { itemLocation ->
        val equipped by remember { derivedStateOf { item.parent.equippedItems.contains(item) } }
        if (equipped) return@IfNotNull

        val transferText = if (itemLocation == editor.view) "transfer" else "pull"
        val tooltip = @Composable {
            TooltipText(
                if (transferText == "transfer") Localizations.UiText("transfer_tooltip", item.where!!.other().localizedName)
                else Localizations.UiText("pull_tooltip", itemLocation.localizedName, editor.view.localizedName)
            )
        }

        AnimatedTooltipArea(
            tooltip = tooltip,
            delayMillis = 0
        ) {
            ItemAlterButton(
                text = Localizations.UiText(transferText, itemLocation.other().localizedName),
                onClick = { item.transfer(editor) }
            )
        }
    }
    Spacer(modifier = Modifier.width(7.dp))
    AnimatedTooltipArea(
        tooltip = { TooltipText(Localizations.UiText("change_type_tooltip")) },
        delayMillis = 0,
    ) {
        ItemAlterButton(
            text = Localizations.UiText("change_type"),
            onClick = {
                // TODO!
                // Arctic.overlayState.itemEdition = item
            }
        )
    }
    Spacer(modifier = Modifier.width(7.dp))
    ItemAlterButton(
        text = Localizations.UiText("duplicate"),
        onClick = {
            // TODO!!
//        if (item.where == editor.view) {
//            if (editor.noSpaceInInventory)
//                Arctic.overlayState.inventoryFull = true
//            else
//                item.parent.addItem(editor, item.copy(), item)
//        } else {
//            Arctic.overlayState.itemDuplication = item
//        }
        }
    )
    Spacer(modifier = Modifier.width(7.dp))
    ItemAlterButton(
        text = Localizations.UiText("delete"),
        color = Color(0x25ff6d0c),
        onClick =  {
            // TODO!!
//        Arctic.overlayState.itemDeletion = item
        }
    )
}

@Composable
private fun ItemNetheriteEnchantButton(holder: Item) {

    @Composable
    fun InactiveItemNetheriteEnchantButton(builder: Retriever<Enchantment>) {
        ItemAlterButton(
            color = Color(0x15ffffff),
            horizontalPadding = 4.dp,
            onClick = {
                val target = builder()
                // TODO!
//        Arctic.overlayState.enchantment = ItemEnchantmentOverlayState(holder, target)
            }
        ) {
            Image(
                bitmap = DungeonsTextures["/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png"],
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    @Composable
    fun ActiveItemNetheriteEnchantButton(enchantment: Enchantment) {
        ItemAlterButton(
            color = Color(0x40ffc847),
            horizontalPadding = 10.dp,
            onClick = {
                val target = enchantment
                // TODO!
//        Arctic.overlayState.enchantment = ItemEnchantmentOverlayState(holder, target)
            }
        ) {
            Image(
                bitmap = enchantment.data.icon,
                contentDescription = null,
                modifier = Modifier
                    .requiredSize(30.dp)
                    .drawBehind {
                        drawImage(
                            image = DungeonsTextures["/Game/Content_DLC4/UI/Materials/Inventory/gilded_bullit.png"],
                            dstSize = IntSize(size.width.toInt(), size.height.toInt())
                        )
                    }
                    .scale(1.05f)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = DungeonsLocalizations["AncientLabels/iteminspector_gilded"]!!, fontSize = 18.sp, color = Color.White)
        }
    }

    val enchantment = holder.netheriteEnchant

    if (enchantment != null && !enchantment.isUnset)
        ActiveItemNetheriteEnchantButton(enchantment)
    else
        InactiveItemNetheriteEnchantButton(builder = { holder.newNetheriteEnchant() })
}

@Composable
private fun ItemModifiedButton(holder: Item) {
    val modified = holder.modified == true
    var timesModified by remember { mutableStateOf("${holder.timesModified ?: 0}") }

    ItemAlterButton(
        color = if (modified) Color(0x556f52ff) else Color(0x15ffffff),
        onClick = { holder.modified = !modified }
    ) {
        Text(
            text = if (modified) Localizations.UiText("modified") else "_",
            fontSize = 18.sp,
            color = Color.White
        )

        if (!modified) return@ItemAlterButton

        TextFieldValidatable(
            value = timesModified,
            onValueChange = { timesModified = it },
            validator = { it.toIntOrNull() != null },
            onSubmit = { holder.timesModified = timesModified.toInt().takeIf { it != 0 } },
            textStyle = TextStyle(fontSize = 18.sp, color = Color.White, textAlign = TextAlign.End),
            hideDecorationIfNotFocused = true,
            modifier = Modifier.requiredWidth(28.dp).padding(end = 2.dp).offset(y = (-1).dp)
        )
        Text(
            text = Localizations.UiText("times"),
            fontSize = 18.sp,
            color = Color.White
        )
    }
}

@Composable
private fun ItemName(text: String) =
    AutosizeText(
        text = text,
        maxFontSize = 60.sp,
        style = LocalTextStyle.current.copy(
            shadow = Shadow(color = Color.Black, offset = Offset.Zero, blurRadius = 5f),
            fontWeight = FontWeight.Bold,
        ),
        modifier = Modifier.padding(bottom = 12.dp)
    )

@Composable
private fun ItemDescription(text: String?) {
    if (text == null) return

    Text(
        text = text,
        fontSize = 25.sp,
        color = Color.White,
        style = TextStyle(shadow = Shadow(color = Color.Black, offset = Offset.Zero, blurRadius = 5f)),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

