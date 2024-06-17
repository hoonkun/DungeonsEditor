package kiwi.hoonkun.ui.composables

import LocalData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import dungeons.DungeonsJsonFile
import dungeons.Localizations
import kiwi.hoonkun.ui.states.Item
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsTextures

@Composable
fun Intro(
    visible: Boolean,
    onPathSelect: (newPath: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize().then(modifier)
    ) {
        Image(
            bitmap = DungeonsTextures["/Game/UI/Materials/LoadingScreens/Loading_Ancient_Hunt.png"],
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    renderEffect = BlurEffect(50.dp.value, 50.dp.value)
                }
                .drawWithContent {
                    drawContent()
                    drawRect(Color.Black.copy(alpha = 0.6f))
                }
        )

        Row(modifier = Modifier.fillMaxSize()) {
            val recent = LocalData.recentSummaries
            val detected = DungeonsJsonFile.detected

            LazyColumn {
                item { Text(Localizations.UiText("recent_files")) }
                items(recent) { (name, json, summary) ->

                }
                item { Text(Localizations.UiText("detected_files")) }
                items(detected) { (name, json, summary) ->

                }
            }
        }
    }
}

@Composable
private fun EquipmentPreview(item: Item) {

}