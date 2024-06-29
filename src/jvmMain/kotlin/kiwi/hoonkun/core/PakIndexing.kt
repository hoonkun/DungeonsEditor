package kiwi.hoonkun.core

import MainMenuButtons
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.Resources
import kiwi.hoonkun.ui.composables.base.FileSelector
import kiwi.hoonkun.ui.composables.overlays.ErrorOverlay
import kiwi.hoonkun.ui.composables.overlays.OverlayRoot
import kiwi.hoonkun.ui.composables.overlays.OverlayTitleDescription
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.states.Overlay
import kiwi.hoonkun.ui.states.OverlayCloser
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.utils.chunkedMerge
import kotlinx.coroutines.*
import minecraft.dungeons.io.DungeonsPakRegistry
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsSkeletons
import kotlin.math.ceil
import kotlin.math.roundToInt


@Composable
fun rememberPakIndexingState(
    initialState: PakIndexingState = PakIndexingState.Idle,
    requestExitApplication: () -> Unit = { }
): State<PakIndexingState> {
    val overlays = LocalOverlayState.current

    val state = remember { mutableStateOf(initialState) }

    LaunchedEffect(state) {
        snapshotFlow { state.value }.collect { pakIndexingState ->
            when (pakIndexingState) {
                is PakIndexingState.Idle -> {
                    overlays.make(
                        canBeDismissed = false,
                        backdropOptions = Overlay.BackdropOptions(alpha = 0.6f)
                    ) { destroy ->
                        PakIndexingOverlay(
                            onStateChanged = { state.value = it },
                            requestClose = destroy
                        )
                    }
                }
                is PakIndexingState.NotFound -> {
                    overlays.make(
                        canBeDismissed = false
                    ) { requestClose ->
                        PakNotFoundOverlay(
                            onSelect = {
                                ArcticSettings.customPakLocation = it
                                ArcticSettings.save()

                                state.value = PakIndexingState.Idle
                            },
                            requestClose = requestClose,
                        )
                    }
                }
                is PakIndexingState.Error -> {
                    overlays.make(
                        canBeDismissed = false
                    ) {
                        ErrorOverlay(
                            error = pakIndexingState.error,
                            title = Localizations["error_pak_title"]
                        )
                    }
                }
                is PakIndexingState.Loaded -> {
                    // noop
                }
            }
        }
    }

    return state
}

sealed interface PakIndexingState {
    data object Idle: PakIndexingState
    data object Loaded: PakIndexingState
    data object NotFound: PakIndexingState
    data class Error(val error: Exception): PakIndexingState
}


@OptIn(DelicateCoroutinesApi::class)
@Composable
private fun PakIndexingOverlay(
    onStateChanged: (PakIndexingState) -> Unit,
    requestClose: OverlayCloser
) {
    var indexingText by remember {
        mutableStateOf(value = PakIndexingText())
    }

    var itemLoadingText by remember {
        mutableStateOf(value = PakIndexingText())
    }
    val itemLoadingProgress = remember {
        List(size = 8) { PakIndexingProgress() }.toMutableStateList()
    }

    var enchantmentLoadingText by remember {
        mutableStateOf(PakIndexingText())
    }
    val enchantmentLoadingProgress = remember {
        List(size = 8) { PakIndexingProgress() }.toMutableStateList()
    }

    LaunchedEffect(Unit) {
        val context = newFixedThreadPoolContext(16, "PakTextureIndexingPool")

        try {
            indexingText = indexingText.copy(leading = Localizations["pak_reading"])

            val initialized = withContext(Dispatchers.IO) {
                DungeonsPakRegistry.initialize(ArcticSettings.customPakLocation)
            }
            if (!initialized)
                return@LaunchedEffect onStateChanged(PakIndexingState.NotFound)

            indexingText = indexingText.copy(
                leading = Localizations["progress_text_reading_localization"]
            )

            DungeonsLocalizations.initialize()

            if (ArcticSettings.preloadTextures) {
                indexingText = indexingText.copy(
                    leading = Localizations["progress_text_reading_textures"]
                )

                val targetItems = PakTextureLoader(DungeonsSkeletons.Item[Unit])
                val targetEnchantments = PakTextureLoader(DungeonsSkeletons.Enchantment[Unit])

                itemLoadingText = itemLoadingText.copy(
                    leading = Localizations["progress_text_item_texture"]
                )
                enchantmentLoadingText = enchantmentLoadingText.copy(
                    leading = Localizations["progress_text_enchantment_texture"]
                )

                val job = launch(context) {
                    targetItems.load(
                        scope = this,
                        progresses = itemLoadingProgress,
                        onEachLoad = {
                            itemLoadingText = itemLoadingText.copy(trailing = it.name)
                        }
                    )
                    targetEnchantments.load(
                        scope = this,
                        progresses = enchantmentLoadingProgress,
                        onEachLoad = {
                            enchantmentLoadingText = enchantmentLoadingText.copy(trailing = it.name)
                        }
                    )
                }

                job.join()
            }

            indexingText = PakIndexingText(
                leading = Localizations["progress_text_completed"],
                trailing = Localizations["cleaning_up"]
            )

            delay(500)
            onStateChanged(PakIndexingState.Loaded)
        } catch (e: Exception) {
            onStateChanged(PakIndexingState.Error(e))
        } finally {
            context.close()
            requestClose()
        }
    }

    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations["pak_indexing_title"],
            description = Localizations["pak_indexing_description"]
        )

        if (!ArcticSettings.preloadTextures) return@OverlayRoot

        Spacer(modifier = Modifier.height(50.dp))

        Column(modifier = Modifier.width(700.dp)) {
            PakIndexingText(text = indexingText)
            Spacer(modifier = Modifier.height(24.dp))
            PakIndexingText(text = itemLoadingText)
            Row {
                itemLoadingProgress
                    .chunkedMerge(2) { it.merge() }
                    .forEach {
                        TextProgressBar(
                            progress = it.progress,
                            modifier = Modifier.weight(1f)
                        )
                    }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PakIndexingText(enchantmentLoadingText)
            Row {
                enchantmentLoadingProgress
                    .chunkedMerge(2) { it.merge() }
                    .forEach {
                        TextProgressBar(
                            progress = it.progress,
                            modifier = Modifier.weight(1f)
                        )
                    }
            }
        }
    }
}

@Composable
private fun PakNotFoundOverlay(
    onSelect: (newPath: String) -> Unit,
    requestClose: OverlayCloser,
) {
    OverlayRoot {
        OverlayTitleDescription(
            title = Localizations["pak_not_found_title"],
            description = Localizations["pak_not_found_description"]
        )
        Spacer(
            modifier = Modifier.height(52.dp)
        )
        FileSelector(
            buttonText = Localizations["select"],
            validator = validator@ {
                if (!it.isDirectory) return@validator false
                val files = it.listFiles() ?: return@validator false
                files.any { file -> file.extension == "pak" }
            },
            onSelect = {
                onSelect(it.absolutePath)
                requestClose()
            },
            modifier = Modifier.width(1050.dp)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp, horizontal = 48.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .scale(1.25f)
        ) {
            MainMenuButtons(description = null)
        }
    }
}


private object TextProgressBarConstants {
    val ProgressStyle = SpanStyle(color = Color(0x40ff8d30))
    val PacmanStyle = SpanStyle(color = Color(0xffffcc00), fontWeight = FontWeight.Bold)
    val RemainingStyle = SpanStyle(color = Color.White.copy(alpha = 0.5f))
}

@Composable
private fun TextProgressBar(progress: Float, width: Int = 11, modifier: Modifier = Modifier) {
    val progressInt = (progress * width).roundToInt()

    val appendProgress: AnnotatedString.Builder.() -> Unit = {
        append("-".repeat(progressInt))
    }
    val appendPacman: AnnotatedString.Builder.() -> Unit = {
        append(if (progressInt % 4 < 2) "C" else "c")
    }
    val appendRemaining: AnnotatedString.Builder.() -> Unit = {
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
            append("[")
            withStyle(TextProgressBarConstants.ProgressStyle) { appendProgress() }
            withStyle(TextProgressBarConstants.PacmanStyle) { appendPacman() }
            withStyle(TextProgressBarConstants.RemainingStyle) { appendRemaining() }
            append("]")
        },
        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontFeatureSettings = "liga 0"),
        fontFamily = Resources.Fonts.JetbrainsMono,
        modifier = Modifier.alpha(if (progressInt == 0) 0.4f else 1f).fillMaxWidth().then(modifier),
    )
}

@Composable
private fun PakIndexingText(text: PakIndexingText) {
    Row(modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(text = text.leading)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = text.trailing, modifier = Modifier.alpha(0.5f))
    }
}

@Immutable
private data class PakIndexingText(
    val leading: String = Localizations["progress_text_waiting"],
    val trailing: String = ""
)

@Immutable
private data class PakIndexingProgress(
    val elapsed: Int = 0,
    val total: Int = 1
) {
    val progress get() = elapsed.toFloat() / total.toFloat()
}

@Stable
private infix fun PakIndexingProgress.progress(add: Int) = copy(elapsed = elapsed + add)

@Stable
private fun List<PakIndexingProgress>.merge() =
    reduce { acc, curr -> PakIndexingProgress(elapsed = acc.elapsed + curr.elapsed, total = acc.total + curr.total) }

private class PakTextureLoader<S: DungeonsSkeletons.Loadable>(
    private val containers: Collection<S>
) {
    fun load(
        scope: CoroutineScope,
        progresses: SnapshotStateList<PakIndexingProgress>,
        onEachLoad: (S) -> Unit
    ) {
        containers
            .chunked(ceil(containers.size / 8f).roundToInt())
            .onEachIndexed { chunkIndex, chunk -> progresses[chunkIndex] = PakIndexingProgress(total = chunk.size) }
            .forEachIndexed { index, chunk ->
                scope.launch {
                    chunk.forEach { item ->
                        onEachLoad(item.also { it.load() })
                        progresses[index] = progresses[index] progress 1
                    }
                }
            }
    }
}
