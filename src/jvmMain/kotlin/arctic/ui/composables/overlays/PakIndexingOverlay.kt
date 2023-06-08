package arctic.ui.composables.overlays

import LocalData
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import arctic.states.arctic
import arctic.ui.unit.dp
import arctic.ui.utils.getValue
import arctic.ui.utils.mutableRefOf
import arctic.ui.utils.setValue
import dungeons.Database
import dungeons.Localizations
import dungeons.PakRegistry
import kotlinx.coroutines.*


val scope = CoroutineScope(Dispatchers.IO)

@Composable
fun PakIndexingOverlay() {
    var stateText by remember { mutableStateOf("") }

    var initialized by remember { mutableRefOf(false) }

    OverlayBackdrop(!arctic.initialized && !arctic.pakNotFound, 0.6f)
    OverlayAnimator(!arctic.initialized && !arctic.pakNotFound) { Content(stateText = stateText) }

    LaunchedEffect(LocalData.customPakLocation) {
        if (initialized) return@LaunchedEffect

        println("initializing PakRegistry")

        initialized = true

        scope.launch {
            stateText = "Pak 파일을 읽고있습니다"

            if (!PakRegistry.initialize()) {
                arctic.initialized = false
                arctic.pakNotFound = true
                initialized = false

                return@launch
            }

            stateText = "현지화 파일을 읽고있습니다"

            Localizations.initialize()

            if (Settings.preloadTextures) {
                for (item in Database.items.filter { it.name != null }) {
                    stateText = "아이템 텍스쳐를 읽고있습니다\n${item.name}"
                    item.inventoryIcon
                    item.largeIcon
                }

                for (enchantment in Database.enchantments) {
                    stateText = "효과 텍스쳐를 읽고있습니다\n${enchantment.name}"
                    enchantment.icon
                    enchantment.shinePattern
                }
            }

            arctic.initialized = true
        }
    }
}

@Composable
private fun Content(stateText: String) {
    ContentRoot {
        OverlayTitleDescription(
            title = "게임 리소스를 읽는 중입니다",
            description = "다소 시간이 걸릴 수 있으니 조금만 기다려주세요."
        )
        if (Settings.preloadTextures) {
            Spacer(modifier = Modifier.height(50.dp))
            OverlayDescriptionText(text = stateText)
        }
    }
}
