package kiwi.hoonkun.ui.reusables

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

fun Modifier.offsetRelative(
    x: Float = 0f,
    y: Float = 0f,
): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(placeable.width, placeable.height) {
            placeable.placeRelative(
                x = (placeable.width * x).toInt(),
                y = (placeable.height * y).toInt()
            )
        }
    }
