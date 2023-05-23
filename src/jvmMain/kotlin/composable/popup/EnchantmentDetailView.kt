package composable.popup

import Localizations
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composable.PopupCloseButton
import composable.inventory.EnchantmentIcon
import composable.inventory.LevelImage
import composable.inventory.LevelImagePositioner
import editorState
import extensions.GameResources
import states.Enchantment

@Composable
fun EnchantmentDetailView(enchantment: Enchantment, requestClose: () -> Unit) {
    val source = remember { MutableInteractionSource() }

    Row {
        Box(modifier = Modifier.size(300.dp).clickable(source, null) { editorState.detailState.toggleEnchantmentSelector() }) {
            PopupCloseButton(requestClose)
            EnchantmentIcon(enchantment)
            LevelImagePositioner { LevelImage(enchantment.level) }
        }
        Column(modifier = Modifier.padding(top = 20.dp, end = 30.dp, bottom = 30.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                NameText(text = Localizations.EnchantmentName(enchantment.data))
                if (enchantment.data.powerful) PowerfulEnchantmentIndicator()
            }
            DescriptionText(text = Localizations.EnchantmentDescription(enchantment.data))
            Spacer(modifier = Modifier.height(20.dp))
            EffectText(text = Localizations.EnchantmentEffect(enchantment.data))
            Spacer(modifier = Modifier.weight(1f))
            LevelAdjustView(enchantment)
        }
    }
}

@Composable
fun PowerfulEnchantmentIndicator() =
    Text(
        text = Localizations["/enchantment_rarity_powerful"]!!,
        style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color(0xffe5247e)),
        modifier = Modifier.padding(start = 10.dp, bottom = 3.dp)
    )

@Composable
private fun LevelAdjustView(enchantment: Enchantment) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SelectableLevelButton(0, enchantment.level) { enchantment.adjustLevel(0) }
        SelectableLevelButton(1, enchantment.level) { enchantment.adjustLevel(1) }
        SelectableLevelButton(2, enchantment.level) { enchantment.adjustLevel(2) }
        SelectableLevelButton(3, enchantment.level) { enchantment.adjustLevel(3) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.SelectableLevelButton(displayLevel: Int, selectedLevel: Int, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .background(if (selectedLevel == displayLevel) Color(if (displayLevel == 0) 0x20ffffff else 0x20b442f6) else Color.Transparent, shape = RoundedCornerShape(5.dp))
            .onClick(onClick = onClick)
    ) {
        if (displayLevel == 0) Image(bitmap = GameResources.image { "/Game/UI/Materials/Inventory2/Enchantment/behind_enchantments_whole_switch.png" }, null, modifier = Modifier.fillMaxSize(0.8f))
        else LevelImage(displayLevel, 3f)
    }
}

@Composable
private fun NameText(text: String) =
    Text(text = text, style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White))

@Composable
private fun DescriptionText(text: String?) {
    if (text == null) return
    Text(text = text, style = TextStyle(fontSize = 18.sp, color = Color.White))
}

@Composable
private fun EffectText(text: String?) {
    if (text == null) return
    Text(text = text, style = TextStyle(fontSize = 20.sp, color = Color.White))
}