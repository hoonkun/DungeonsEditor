package arctic.ui.composables.overlays

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import arctic.ui.composables.overlays.extended.ComplicatedOverlays
import arctic.ui.unit.dp
import arctic.ui.unit.sp


@Composable
fun Overlays() {

    ComplicatedOverlays()

    FileSaveOverlay()
    FileLoadOverlay()

    FileCloseConfirmOverlay()
    FileOpenFailureOverlay()

    InventoryFullOverlay()

    ItemDeletionConfirmOverlay()
    ItemDuplicateLocationConfirmOverlay()

    PakIndexingOverlay()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> OverlayAnimator(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn() + scaleIn(initialScale = 1.1f)
            val exit = fadeOut() + scaleOut(targetScale = 1.1f)
            enter with exit
        },
        modifier = Modifier.fillMaxSize(),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OverlayAnimator(visible: Boolean, content: @Composable AnimatedVisibilityScope.() -> Unit) =
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 1.1f),
        exit = fadeOut() + scaleOut(targetScale = 1.1f),
        content = content
    )

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverlayBackdrop(visible: Boolean, alpha: Float = 0.3764f, onClick: () -> Unit = {  }) =
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), label = "Backdrop") {
        Box(modifier = Modifier.fillMaxSize().alpha(alpha).background(Color(0xff000000)).onClick(onClick = onClick))
    }

@Composable
fun OverlayTitleText(text: String) =
    Text(
        text = text,
        color = Color.White,
        fontSize = 32.sp
    )

@Composable
fun OverlayDescriptionText(text: String) =
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.4f),
        fontSize = 24.sp,
        textAlign = TextAlign.Center
    )

@Composable
fun OverlayTitleDescription(
    title: String,
    description: String? = null
) {
    OverlayTitleText(title)
    if (description == null) return

    Spacer(modifier = Modifier.height(20.dp))
    OverlayDescriptionText(description)
}

@Composable
fun ContentRoot(content: @Composable ColumnScope.() -> Unit) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
        content = content
    )

@Composable
fun SizeMeasureDummy() = Box(modifier = Modifier.fillMaxSize())

fun whereName(where: String): String = if (where == "inventory") "인벤토리" else "창고"
