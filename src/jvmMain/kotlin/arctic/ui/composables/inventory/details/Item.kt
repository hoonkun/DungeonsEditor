package arctic.ui.composables.inventory.details

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import arctic.states.arctic
import arctic.ui.composables.atomic.ItemAlterButton
import arctic.ui.composables.atomic.ItemRarityButton
import arctic.ui.composables.atomic.PowerEditField
import arctic.ui.composables.atomic.UnlabeledField
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.DungeonsPower
import dungeons.IngameImages
import dungeons.Localizations
import dungeons.states.Enchantment
import dungeons.states.Item
import dungeons.states.extensions.addItem
import dungeons.states.extensions.data
import dungeons.states.extensions.where
import extensions.toFixed

@Composable
fun ItemDetail(item: Item?) {
    RootAnimator(item) {
        if (it != null) Content(it)
        else Box(modifier = Modifier.fillMaxWidth())
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
        content = content
    )

private fun Content(item: Item) {
    Box(modifier = Modifier.wrapContentHeight().fillMaxWidth()) {
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
            Row {
                ItemRarityButton(item.data, item.rarity) { item.rarity = it }
                if (item.data.variant != "Artifact") {
                    Spacer(modifier = Modifier.width(7.dp))
                    ItemNetheriteEnchantButton(item)
                    Spacer(modifier = Modifier.width(7.dp))
                    ItemModifiedButton(item)
                }
                Spacer(modifier = Modifier.weight(1f))
                ItemAlterButton("타입 변경") { arctic.edition.enable(item) }
                Spacer(modifier = Modifier.width(7.dp))
                ItemAlterButton("복제") {
                    if (item.where == arctic.view) {
                        if (!arctic.alerts.checkAvailable())
                            item.parent.addItem(item.copy(), item)
                    } else {
                        arctic.duplication.target = item
                    }
                }
                Spacer(modifier = Modifier.width(7.dp))
                ItemAlterButton("삭제") { arctic.deletion.target = item }
            }

            ItemName(item.data.name ?: "알 수 없는 아이템")

            ItemDescription(item.data.flavour)
            ItemDescription(item.data.description)

            Spacer(modifier = Modifier.height(20.dp))

            val armorProperties = item.armorProperties
            if (armorProperties != null) {
                ItemArmorProperties(item, armorProperties)
            }

            PowerEditField(
                value = DungeonsPower.toInGamePower(item.power).toFixed(4).toString(),
                onValueChange = {
                    if (it.toDoubleOrNull() != null) item.power = DungeonsPower.toSerializedPower(it.toDouble())
                }
            )

            val enchantments = item.enchantments
            if (enchantments != null) {
                Spacer(modifier = Modifier.height(40.dp))
                ItemEnchantments(enchantments)
            }
        }
    }
}

@Composable
private fun ItemNetheriteEnchantButton(holder: Item) {
    val enchantment = holder.netheriteEnchant

    val onClick = {
        val target = enchantment
            ?: Enchantment(holder, "Unset", isNetheriteEnchant = true).also { holder.netheriteEnchant = it }
        arctic.enchantments.viewDetail(target)
    }

    ItemAlterButton(
        color = Color(if (enchantment == null || enchantment.id == "Unset") 0x15ffffff else 0x40ffc847),
        onClick = onClick
    ) {
        if (enchantment == null || enchantment.id == "Unset") {
            Image(
                bitmap = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment2/locked_enchantment_slot.png" },
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        } else {
            Image(
                bitmap = enchantment.data.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .drawBehind {
                        drawImage(IngameImages.get { "/Game/Content_DLC4/UI/Materials/Inventory/enchantSpecialUnique_Bullit.png" })
                    }
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = Localizations["AncientLabels/iteminspector_gilded"]!!, fontSize = 20.sp, color = Color.White)
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
        Text(text = if (modified) "효과 변경" else "_", fontSize = 20.sp, color = Color.White)
        if (!modified) return@ItemAlterButton
        UnlabeledField("${holder.timesModified ?: 0}") { newValue ->
            if (newValue.toIntOrNull() != null)
                holder.timesModified = newValue.toInt().takeIf { it != 0 }
        }
        Text(text = "번", fontSize = 20.sp, color = Color.White)
    }
}

@Composable
private fun ItemName(text: String) =
    Text(
        text = text,
        fontSize = 60.sp,
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

