package arctic.ui.composables.overlays

import LocalData
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import arctic.states.arctic
import arctic.ui.composables.fonts.JetbrainsMono
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import arctic.ui.utils.getValue
import arctic.ui.utils.mutableRefOf
import arctic.ui.utils.setValue
import dungeons.Database
import dungeons.Localizations
import dungeons.PakRegistry
import kotlinx.coroutines.*
import kotlin.math.roundToInt


val scope = CoroutineScope(Dispatchers.IO)

@Composable
fun PakIndexingOverlay() {
    var stateText by remember { mutableStateOf("") }
    var progressText by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0) }
    var totalProgress by remember { mutableStateOf(1) }

    var initialized by remember { mutableRefOf(false) }

    OverlayBackdrop(!arctic.initialized && !arctic.pakNotFound, 0.6f)
    OverlayAnimator(!arctic.initialized && !arctic.pakNotFound) {
        Content(stateText = stateText, progressText = progressText, progress = progress, totalProgress = totalProgress)
    }

    LaunchedEffect(LocalData.customPakLocation) {
        if (initialized) return@LaunchedEffect

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
                totalProgress = Database.items.size + Database.enchantments.size

                for (item in Database.items.filter { it.name != null }) {
                    stateText = "아이템 텍스쳐를 읽고있습니다"
                    progressText = item.name!!
                    progress++
                    item.inventoryIcon
                    item.largeIcon
                }

                for (enchantment in Database.enchantments) {
                    stateText = "효과 텍스쳐를 읽고있습니다"
                    progressText = enchantment.name
                    progress++
                    enchantment.icon
                    enchantment.shinePattern
                }
            }

            progress++
            stateText = "완료되었습니다!"
            progressText = "정리 중"

            delay(500)

            arctic.initialized = true
        }
    }
}

@Composable
private fun Content(stateText: String, progressText: String, progress: Int, totalProgress: Int) {
    val progressPercentage = (progress.toFloat() / totalProgress.toFloat() * 56).roundToInt()

    ContentRoot {
        OverlayTitleDescription(
            title = "게임 리소스를 읽는 중입니다",
            description = "다소 시간이 걸릴 수 있으니 조금만 기다려주세요."
        )
        if (Settings.preloadTextures) {
            Spacer(modifier = Modifier.height(50.dp))
            Column(modifier = Modifier.width(696.dp)) {
                Row {
                    Text(text = stateText, color = Color.White, fontSize = 20.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White.copy(alpha = 0.5f))) { append(progressText) }
                            append("   ")
                            withStyle(SpanStyle(color = Color.White)) { append("${(progress.toFloat() / totalProgress.toFloat() * 100).roundToInt()}% ") }
                        },
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) { append("[") }
                        withStyle(SpanStyle(color = Color(0x40ff8d30))) { append("-".repeat(progressPercentage)) }
                        withStyle(SpanStyle(color = Color(0xffffcc00), fontWeight = FontWeight.Bold)) { append(if (progress % 4 < 2) "C" else "c") }
                        withStyle(SpanStyle(color = Color.White.copy(alpha = 0.5f))) {
                            val remaining = 56 - progressPercentage
                            if (remaining % 2 == 0) {
                                append("o ".repeat(remaining / 2 - 1))
                                append("o")
                            } else {
                                append(" o".repeat(remaining / 2))
                            }
                        }
                        withStyle(SpanStyle(color = Color.White)) { append("]") }
                    },
                    fontSize = 20.sp,
                    fontFamily = JetbrainsMono,
                    modifier = Modifier.alpha(if (progress == 0) 0.4f else 1f),
                    style = TextStyle(
                        fontFeatureSettings = "liga 0"
                    )
                )
            }
        }
    }
}
