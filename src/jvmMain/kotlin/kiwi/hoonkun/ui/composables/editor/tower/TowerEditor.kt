package kiwi.hoonkun.ui.composables.editor.tower

import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import kiwi.hoonkun.ui.reusables.MinimizableAnimatedContent
import kiwi.hoonkun.ui.reusables.defaultFadeIn
import kiwi.hoonkun.ui.reusables.defaultFadeOut
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.states.MutableDungeons

@Composable
fun TowerEditor(
    state: MutableDungeons.TowerMissionState,
    hasInitialTower: Boolean,
    editor: EditorState
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 170.dp)
    ) {

        // TODO

        MinimizableAnimatedContent(
            targetState = editor.data.includeEditedTower,
            transitionSpec = {
                val enter = defaultFadeIn() + slideIn { IntOffset(-30.dp.value.toInt(), 0) }
                val exit = defaultFadeOut() + slideOut { IntOffset(-30.dp.value.toInt(), 0) }

                enter togetherWith exit using SizeTransform(false)
            },
            modifier = Modifier.align(Alignment.BottomStart),
        ) {
            IncludeEditedTowerDataSwitcher(
                currentValue = it,
                onClick = { newValue -> editor.data.includeEditedTower = newValue },
            )
        }

        ExperimentalFeatureWarning(
            hasInitialTower = hasInitialTower,
            modifier = Modifier.align(Alignment.BottomEnd)
        )

    }

}

@Composable
fun IncludeEditedTowerDataSwitcher(
    currentValue: Boolean,
    onClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            buildAnnotatedString {
                append("수정한 탑 데이터를 ")

                withLink(
                    LinkAnnotation.Clickable(
                        tag = "include_edited_tower",
                        styles = TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline)),
                    ) {
                        onClick(!currentValue)
                    }
                ) {
                    append(
                        if (!currentValue) "사용합니다"
                        else "사용하지 않습니다."
                    )
                }
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (!currentValue) "현재 보이는 데이터를 저장 시 반영합니다."
            else "수정한 데이터를 반영하지 않고 기존 데이터를 유지합니다.",
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ExperimentalFeatureWarning(
    hasInitialTower: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Text(
            if (hasInitialTower) "현재 기존 탑 데이터를 수정하고 있습니다."
            else "현재 새 탑 데이터를 추가하고 있습니다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (hasInitialTower) "실험적 기능입니다. 일부 기존 데이터가 유실되거나 게임이 제대로 동작하지 않을 수 있습니다."
            else "실험적 기능입니다. 로드 시 게임이 제대로 동작하지 않을 수 있습니다.",
            color = Color(0xffff884c)
        )
    }
}
