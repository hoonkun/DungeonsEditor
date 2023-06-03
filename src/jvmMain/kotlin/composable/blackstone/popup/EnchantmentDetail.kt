package composable.blackstone.popup

import dungeons.Localizations
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import blackstone.states.Enchantment
import blackstone.states.items.data
import blackstone.states.items.leveling
import composable.inventory.EnchantmentIcon
import composable.inventory.LevelImage
import composable.inventory.LevelImagePositioner
import dungeons.IngameImages


@Composable
fun EnchantmentDetail(enchantment: Enchantment) {
    Debugging.recomposition("EnchantmentDetailView")

    Row(modifier = Modifier.requiredSize(675.dp, 300.dp).background(Color(0xff080808))) {
        Box(modifier = Modifier.fillMaxHeight().aspectRatio(1f / 1f)) {
            EnchantmentIcon(enchantment, scale = 1.0f)
            LevelImagePositioner(size = 0.4f) { LevelImage(enchantment.level, scale = 1.5f) }
        }
        Column(modifier = Modifier.padding(top = 20.dp, end = 30.dp, bottom = 30.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                NameText(text = enchantment.data.name)
                if (enchantment.data.powerful) PowerfulEnchantmentIndicator()
            }
            DescriptionText(text = enchantment.data.description)
            Spacer(modifier = Modifier.height(20.dp))
            EffectText(text = enchantment.data.effect)

            if (enchantment.id != "Unset") {
                Spacer(modifier = Modifier.weight(1f))
                LevelAdjustView(enchantment)
            }
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
private fun LevelAdjustView(enchantment: Enchantment) =
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!enchantment.isNetheriteEnchant)
            SelectableLevelButton(0, enchantment.level) { enchantment.leveling(0) }
        SelectableLevelButton(1, enchantment.level) { enchantment.leveling(1) }
        SelectableLevelButton(2, enchantment.level) { enchantment.leveling(2) }
        SelectableLevelButton(3, enchantment.level) { enchantment.leveling(3) }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.SelectableLevelButton(displayLevel: Int, selectedLevel: Int, onClick: () -> Unit) {
    Debugging.recomposition("SelectableLevelButton")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .background(if (selectedLevel == displayLevel) Color(if (displayLevel == 0) 0x20ffffff else 0x20b442f6) else Color.Transparent, shape = RoundedCornerShape(5.dp))
            .onClick(onClick = onClick)
    ) {
        if (displayLevel == 0) Image(bitmap = IngameImages.get { "/Game/UI/Materials/Inventory2/Enchantment/behind_enchantments_whole_switch.png" }, null, modifier = Modifier.fillMaxSize(0.8f))
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