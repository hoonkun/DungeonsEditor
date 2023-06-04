package arctic.ui.composables.atomic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import arctic.ui.unit.dp
import dungeons.IngameImages
import dungeons.Localizations

@Composable
fun PowerEditField(value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        PowerIcon()
        Spacer(modifier = Modifier.width(10.dp))
        LabeledField(label = Localizations["/gearpower_POWER"]!!, value = value, onValueChange = onValueChange)
    }
}

@Composable
private fun PowerIcon() =
    Image(
        bitmap = IngameImages.get { "/Game/UI/Materials/MissionSelectMap/inspector/gear/powericon.png" },
        contentDescription = null,
        modifier = Modifier.size(30.dp)
    )
