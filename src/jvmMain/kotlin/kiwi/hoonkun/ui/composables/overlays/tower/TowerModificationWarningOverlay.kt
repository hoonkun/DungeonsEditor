package kiwi.hoonkun.ui.composables.overlays.tower

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.composables.overlays.OverlayRoot
import kiwi.hoonkun.ui.composables.overlays.OverlayTitleDescription
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp

@Composable
fun TowerModificationWarningOverlay(
    onConfirm: () -> Unit,
    requestClose: () -> Unit
) {
    OverlayRoot {
        OverlayTitleDescription(
            title = "타워를 수정하려고 합니다",
        )

        WarningContainer {
            WarningRed("계정이 정지당할 가능성이 높습니다.")
            WarningDescription("개발자가 탑 수정 후 플레이 중 '게임 소유권 확인 실패'의 형태로 계정이 정지당한 정황을 확인했습니다.")
            WarningWhite("수정한 타워는 높은 확률로 제대로 동작하지 않습니다.")
            WarningDescription("일부 미완성인 내부용 지형 및 도전이 존재하며, 잘못된 데이터가 들어가면 게임이 무시하고 새 데이터를 만들어 덮어씌웁니다.")
            Spacer(modifier = Modifier.height(0.dp))
            WarningTurn("그럼에도 수정하려고 하신다면:")
            Spacer(modifier = Modifier.height(0.dp))
            WarningRed("연구용 부계정을 만드십시오.")
            WarningDescription("탑을 수정하는 이상 언제든지 계정이 정지당해도 이상하지 않습니다.\n세이브 데이터가 정지당하는 것이 아닌 계정이 정지당하여 해당 마이크로소프트 계정으로 로그인했을 때 시즌보상 등의 제약을 받습니다.")
            WarningRed("수정한 타워를 30층까지 완료하지 마십시오.")
            WarningDescription("타워 보상은 충분히 기존 아이템 수정으로도 안전하게 추가할 수 있습니다.")
            WarningWhite("전투 층에는 반드시 도전이 있어야 합니다.")
            WarningDescription("그러지 않으면 출구가 정상적으로 생성되지 않아 층에 갇힙니다.")
            WarningWhite("상인 층의 지형은 반드시 '관계자실' 1, 2, 3중 하나여야 합니다.")
            WarningDescription("그래야 지형의 일정한 위치에 상인이 스폰될 수 있습니다.")
            Spacer(modifier = Modifier.height(0.dp))
            WarningTurn("이후 '계속 수정'을 눌러 발생한 모든 데이터 손실 및 계정에 발생한 문제에 대해 개발자는 책임지지 않습니다.", color = Color(0xffffc14f))
        }
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            RetroButton(
                text = "뒤로 가기",
                color = Color(0xffffffff),
                hoverInteraction = RetroButtonHoverInteraction.Overlay,
                onClick = requestClose
            )
            Spacer(modifier = Modifier.width(75.dp))
            RetroButton(
                text = "계속 수정",
                color = Color(0xffff6e25),
                hoverInteraction = RetroButtonHoverInteraction.Outline,
                onClick = onConfirm
            )
        }
    }
}

@Composable
fun WarningContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 64.dp).width(900.dp)
    ) {
        content()
    }
}

@Composable
fun WarningWhite(text: String) {
    Text(text = text, color = Color.White, fontSize = 22.sp)
}

@Composable
fun WarningRed(text: String) {
    Text(text = text, color = Color(0xffff6e25), fontWeight = FontWeight.Bold, fontSize = 22.sp)
}

@Composable
fun WarningDescription(text: String) {
    Text(text = text, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 16.dp))
}

@Composable
fun WarningTurn(text: String, color: Color = Color.White.copy(alpha = 0.5f)) {
    Text(
        text = text,
        color = color,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp),
    )
}
