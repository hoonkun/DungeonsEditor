package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.Resources
import kiwi.hoonkun.ui.states.OverlayCloser
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import minecraft.dungeons.io.DungeonsPakRegistry
import minecraft.dungeons.resources.DungeonsDatabase
import minecraft.dungeons.resources.DungeonsLocalizations
import kotlin.math.ceil
import kotlin.math.roundToInt


@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
@Composable
fun PakIndexingOverlay(
    onLoaded: () -> Unit,
    onNotFound: () -> Unit,
    onError: (e: Exception) -> Unit,
    requestClose: OverlayCloser
) {
    val threadScope = rememberCoroutineScope { newFixedThreadPoolContext(8, "PakTextureIndexingPool") }

    var initState by remember { mutableStateOf(LoadState()) }

    var itemState by remember { mutableStateOf(LoadState()) }
    val itemProgresses = remember { List(4) { LoadProgress() }.toMutableStateList() }

    var enchantmentState by remember { mutableStateOf(LoadState()) }
    val enchantmentProgresses = remember { List(4) { LoadProgress() }.toMutableStateList() }

    val customPakLocation = ArcticSettings.customPakLocation

    LaunchedEffect(customPakLocation) {
        val mutex = Mutex()

        fun <S>Set<S>.loadEach(
            progresses: SnapshotStateList<LoadProgress>,
            loader: (S) -> Unit,
            then: (S) -> Unit
        ) {
            this.chunked(ceil(size / 4f).roundToInt())
                .also {
                    it.forEachIndexed { chunkIndex, chunk ->
                        progresses[chunkIndex] = progresses[chunkIndex].copy(total = chunk.size)
                    }
                }
                .flatMap { it.chunked(ceil(it.size / 2f).roundToInt()) }
                .forEachIndexed { index, chunk ->
                    threadScope.launch {
                        chunk.forEach { item ->
                            loader(item)
                            mutex.withLock {
                                progresses[index / 2] = progresses[index / 2].let { it.copy(elapsed = it.elapsed + 1) }
                                then(item)
                            }
                        }
                    }
                }
        }

        withContext(Dispatchers.IO) {
            try {
                initState = initState.copy(leadingText = "Pak 파일을 읽고있습니다")

                if (!DungeonsPakRegistry.initialize(customPakLocation)) {
                    onNotFound()
                    return@withContext
                }

                initState = initState.copy(leadingText = Localizations.UiText("progress_text_reading_localization"))

                DungeonsLocalizations.initialize()

                if (ArcticSettings.preloadTextures) {
                    initState = initState.copy(leadingText = Localizations.UiText("progress_text_reading_textures"))

                    val targetItems = DungeonsDatabase.items.toSet()
                    val targetEnchantments = DungeonsDatabase.enchantments

                    itemState = itemState.copy(leadingText = Localizations.UiText("progress_text_item_texture"))
                    enchantmentState =
                        enchantmentState.copy(leadingText = Localizations.UiText("progress_text_enchantment_texture"))

                    targetItems.loadEach(
                        itemProgresses,
                        loader = { it.load() },
                        then = { itemState = itemState.copy(trailingText = it.name) }
                    )
                    targetEnchantments.loadEach(
                        enchantmentProgresses,
                        loader = { it.load() },
                        then = { enchantmentState = enchantmentState.copy(trailingText = it.name) }
                    )

                    while (itemProgresses.any { !it.completed } || enchantmentProgresses.any { !it.completed })
                        yield()
                }

                initState = LoadState(
                    leadingText = Localizations.UiText("progress_text_completed"),
                    trailingText = Localizations.UiText("cleaning_up")
                )

                delay(500)
                onLoaded()
            } catch (e: Exception) {
                onError(e)
            } finally {
                requestClose()
            }
        }
    }

    DisposableEffect(Unit) { onDispose { threadScope.coroutineContext[CloseableCoroutineDispatcher]?.close() } }

    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("pak_indexing_title"),
            description = Localizations.UiText("pak_indexing_description")
        )

        if (!ArcticSettings.preloadTextures) return@OverlayRoot

        Spacer(modifier = Modifier.height(50.dp))
        Column(modifier = Modifier.width(700.dp)) {
            LoadStateText(initState)
            Spacer(modifier = Modifier.height(24.dp))

            LoadStateText(itemState)
            Row { itemProgresses.forEach { TextProgressBar(it.progress, modifier = Modifier.weight(1f)) } }
            Spacer(modifier = Modifier.height(12.dp))
            LoadStateText(enchantmentState)
            Row { enchantmentProgresses.forEach { TextProgressBar(it.progress, modifier = Modifier.weight(1f)) } }
        }
    }
}

@Composable
private fun TextProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val width = 11
    val progressInt = (progress * width).roundToInt()

    val appendProgress: StringAppender = { append("-".repeat(progressInt)) }
    val appendPacman: StringAppender = { append(if (progressInt % 4 < 2) "C" else "c") }
    val appendRemaining: StringAppender = {
        val remaining = width - progressInt
        if (remaining % 2 == 0) {
            append(" o".repeat(remaining / 2))
        } else {
            append("o ".repeat((remaining - 1) / 2))
            append("o")
        }
    }

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White)) { append("[") }
            withStyle(SpanStyle(color = Color(0x40ff8d30))) { appendProgress() }
            withStyle(SpanStyle(color = Color(0xffffcc00), fontWeight = FontWeight.Bold)) { appendPacman() }
            withStyle(SpanStyle(color = Color.White.copy(alpha = 0.5f))) { appendRemaining() }
            withStyle(SpanStyle(color = Color.White)) { append("]") }
        },
        fontSize = 20.sp,
        fontFamily = Resources.Fonts.JetbrainsMono,
        modifier = Modifier.alpha(if (progressInt == 0) 0.4f else 1f).fillMaxWidth().then(modifier),
        style = TextStyle(
            textAlign = TextAlign.Center,
            fontFeatureSettings = "liga 0"
        )
    )
}

@Composable
private fun LoadStateText(state: LoadState) {
    Row(modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(text = state.leadingText, color = Color.White, fontSize = 20.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = state.trailingText, color = Color.White.copy(alpha = 0.5f), fontSize = 20.sp)
    }
}

@Immutable
private data class LoadState(
    val leadingText: String = Localizations.UiText("progress_text_waiting"),
    val trailingText: String = ""
)

@Immutable
private data class LoadProgress(
    val elapsed: Int = 0,
    val total: Int = 1
) {
    val progress get() = elapsed.toFloat() / total.toFloat()
    val completed get() = progress == 1f
}

private typealias StringAppender = AnnotatedString.Builder.() -> Unit
