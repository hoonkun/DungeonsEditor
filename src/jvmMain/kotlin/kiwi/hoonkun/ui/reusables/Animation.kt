package kiwi.hoonkun.ui.reusables

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.compose.ui.util.fastMaxOfOrNull
import kiwi.hoonkun.ArcticSettings

fun <T>defaultTween() = tween<T>(250)

fun defaultFadeIn() = fadeIn(defaultTween())
fun defaultFadeOut() = fadeOut(defaultTween())

@Composable
fun minimizableAnimateFloatAsState(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = spring(),
): State<Float> =
    if (ArcticSettings.minimizeAnimations) {
        remember(targetValue) { mutableStateOf(targetValue) }
    } else {
        animateFloatAsState(targetValue, animationSpec)
    }

@Composable
fun minimizableAnimateDpAsState(
    targetValue: Dp,
    animationSpec: AnimationSpec<Dp> = spring(),
): State<Dp> =
    if (ArcticSettings.minimizeAnimations) {
        remember(targetValue) { mutableStateOf(targetValue) }
    } else {
        animateDpAsState(targetValue, animationSpec)
    }

@Composable
fun minimizableAnimateColorAsState(
    targetValue: Color,
    animationSpec: AnimationSpec<Color> = spring(),
): State<Color> =
    if (ArcticSettings.minimizeAnimations) {
        remember(targetValue) { mutableStateOf(targetValue) }
    } else {
        animateColorAsState(targetValue, animationSpec)
    }

fun <T>minimizableSpecDefault(): AnimationSpec<T> =
    if (ArcticSettings.minimizeAnimations) snap() else spring()

fun <T>minimizableSpec(block: () -> AnimationSpec<T>) =
    if (ArcticSettings.minimizeAnimations) snap() else block()

fun <T>minimizableFiniteSpec(block: () -> FiniteAnimationSpec<T>) =
    if (ArcticSettings.minimizeAnimations) snap() else block()

fun minimizableEnterTransition(block: () -> EnterTransition) =
    if (ArcticSettings.minimizeAnimations) EnterTransition.None else block()

fun minimizableExitTransition(block: () -> ExitTransition) =
    if (ArcticSettings.minimizeAnimations) ExitTransition.None else block()

fun <S>minimizableContentTransform(
    block: AnimatedContentTransitionScope<S>.() -> ContentTransform
): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
    if (ArcticSettings.minimizeAnimations)
        EnterTransition.None togetherWith ExitTransition.None using SizeTransform { _, _ -> snap() }
    else
        block()
}

@Composable
fun <S>MinimizableAnimatedContent(
    targetState: S,
    transitionSpec: AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        val enter = fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90))
        val exit = fadeOut(animationSpec = tween(90))

        enter togetherWith exit
    },
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "AnimatedContent",
    contentKey: (targetState: S) -> Any? = { it },
    modifier: Modifier = Modifier,
    content: @Composable (targetState: S) -> Unit,
) {
    if (!ArcticSettings.minimizeAnimations) {
        AnimatedContent(
            targetState = targetState,
            transitionSpec = transitionSpec,
            contentAlignment = contentAlignment,
            label = label,
            contentKey = contentKey,
            modifier = modifier,
            content = { content(it) }
        )
    } else {
        Layout(
            measurePolicy = remember { MinimizableAnimatedContentBoxMeasurePolicy() },
            modifier = modifier,
            content = { content(targetState) }
        )
    }
}

private class MinimizableAnimatedContentBoxMeasurePolicy : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        val placeables = measurables.fastMap { it.measure(constraints) }
        val maxWidth = placeables.fastMaxBy { it.width }?.width ?: 0
        val maxHeight = placeables.fastMaxBy { it.height }?.height ?: 0

        return layout(maxWidth, maxHeight) {
            placeables.fastForEach {
                it.place(0, 0)
            }
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int
    ) = measurables.fastMaxOfOrNull { it.minIntrinsicWidth(height) } ?: 0

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int
    ) = measurables.fastMaxOfOrNull { it.minIntrinsicHeight(width) } ?: 0

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int
    ) = measurables.fastMaxOfOrNull { it.maxIntrinsicWidth(height) } ?: 0

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int
    ) = measurables.fastMaxOfOrNull { it.maxIntrinsicHeight(width) } ?: 0
}

@Composable
fun MinimizableAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandIn(),
    exit: ExitTransition = shrinkOut() + fadeOut(),
    label: String = "AnimatedVisibility",
    content: @Composable AnimatedVisibilityScope?.() -> Unit
) {
    if (!ArcticSettings.minimizeAnimations) {
         AnimatedVisibility(
            visible = visible,
            modifier = modifier,
            enter = enter,
            exit = exit,
            label = label,
            content = content,
        )
    } else {
        Box(
            modifier = modifier,
            content = { if (visible) content(null) }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun Modifier.minimizableAnimateEnterExit(
    scope: AnimatedVisibilityScope?,
    enter: EnterTransition = fadeIn() + expandIn(),
    exit: ExitTransition = fadeOut() + shrinkOut(),
    label: String = "animateEnterExit"
) =
    if (scope != null) with(scope) { this@minimizableAnimateEnterExit.animateEnterExit(enter, exit, label) }
    else this
