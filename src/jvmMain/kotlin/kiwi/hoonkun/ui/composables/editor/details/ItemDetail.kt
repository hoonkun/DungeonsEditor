package kiwi.hoonkun.ui.composables.editor.details

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.round
import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.*
import kiwi.hoonkun.ui.composables.overlays.*
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.values.DungeonsPower

@Composable
fun ItemDetail(item: Item?, editor: EditorState) {
    MinimizableAnimatedContent(
        targetState = item,
        transitionSpec = minimizableContentTransform spec@ {
            val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter togetherWith exit using SizeTransform(clip = false)
        },
        contentAlignment = Alignment.Center
    ) {
        if (it != null) Content(item = it, editor = editor)
        else Spacer(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Content(item: Item, editor: EditorState) {
    val overlays = LocalOverlayState.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeightIn(min = 450.dp)
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            ItemRarityButton(item.data, item.rarity) { item.rarity = it }
            if (item.data.variant != "Artifact") {
                Spacer(modifier = Modifier.width(7.dp))
                ItemNetheriteEnchantButton(
                    holder = item,
                    enchantment = item.netheriteEnchant
                ) { enchantment ->
                    overlays.make(enter = defaultFadeIn(), exit = defaultFadeOut()) {
                        EnchantmentOverlay(
                            initialSelected = enchantment,
                            requestClose = { overlays.destroy(it) }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(7.dp))
                ItemModifiedButton(item)
            }
        }

        ItemName(item.data.name)
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
                hideLabel = ArcticSettings.locale == "en"
            )
            Spacer(modifier = Modifier.weight(1f))
            ItemAlterRight(item, editor)
        }

        IfNotNull(item.enchantments) {
            Spacer(modifier = Modifier.height(30.dp))
            ItemEnchantments(EnchantmentsHolder(it), modifier = Modifier.fillMaxWidth())
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
                overlays.make(
                    enter = defaultFadeIn(),
                    exit = defaultFadeOut()
                ) {
                    ItemOverlay(
                        state = remember { ItemOverlayEditState(target = item) },
                        requestClose = { overlays.destroy(it) }
                    )
                }
            }
        )
    }
    Spacer(modifier = Modifier.width(7.dp))
    ItemAlterButton(
        text = Localizations.UiText("duplicate"),
        onClick = {
            if (item.where == editor.view) {
                if (editor.view == EditorState.EditorView.Inventory && editor.noSpaceInInventory)
                    overlays.make { InventoryFullOverlay() }
                else
                    item.parent.addItem(editor, item.copy(), item)
            } else {
                overlays.make {
                    ItemDuplicateLocationConfirmOverlay(
                        editor = editor,
                        target = item,
                        requestClose = { overlays.destroy(it) }
                    )
                }
            }
        }
    )
    Spacer(modifier = Modifier.width(7.dp))
    ItemAlterButton(
        text = Localizations.UiText("delete"),
        color = Color(0x25ff6d0c),
        onClick =  {
            overlays.make {
                ItemDeleteConfirmOverlay(
                    editor = editor,
                    target = item,
                    requestClose = { overlays.destroy(it) }
                )
            }
        }
    )
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

