package arctic.ui.composables.inventory

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import arctic.states.arctic
import arctic.ui.composables.inventory.collections.EquippedItemCollection
import arctic.ui.composables.inventory.collections.UnequippedItemCollection
import arctic.ui.composables.inventory.details.ItemDetail
import arctic.ui.composables.overlays.SizeMeasureDummy
import arctic.ui.composables.overlays.extended.tween250
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import dungeons.states.DungeonsJsonState
import dungeons.states.Item

@Composable
fun InventoryView() {
    RootAnimator(arctic.stored) { stored ->
        if (stored != null) Content(stored)
        else SizeMeasureDummy()
    }
}

@Composable
private fun Content(stored: DungeonsJsonState) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        LeftAreaAnimator(stored to arctic.view) { (stored, view) ->
            LeftArea {
                if (view == "inventory") {
                    EquippedItemCollection(stored.equippedItems)
                    Divider()
                    UnequippedItemCollection(stored.unequippedItems)
                } else if (view == "storage") {
                    UnequippedItemCollection(stored.storageChestItems)
                }
            }
        }
        RightAreaAnimator(arctic.selection.selected.any { item -> item != null }) {
            RightArea {
                if (it) ItemComparator(arctic.selection.selected)
                else SomeTips()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S>RootAnimator(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn(tween250()) + slideIn(tween250(), initialOffset = { IntOffset(0, -50.dp.value.toInt()) })
            val exit = fadeOut(tween250()) + slideOut(tween250(), targetOffset = { IntOffset(0, -50.dp.value.toInt()) })
            enter with exit using SizeTransform(false)
        },
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S>LeftAreaAnimator(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn(tween250()) + slideIn(tween250(), initialOffset = { IntOffset(50.dp.value.toInt(), 0) })
            val exit = fadeOut(tween250()) + slideOut(tween250(), targetOffset = { IntOffset(-50.dp.value.toInt(), 0) })
            enter with exit using SizeTransform(false)
        },
        modifier = Modifier.width(657.dp),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun <S>RightAreaAnimator(targetState: S, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { -it / 10 }) + fadeOut()
            enter with exit
        },
        modifier = Modifier.fillMaxHeight().width(718.dp),
        content = content
    )


@Composable
private fun LeftArea(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 20.dp),
        content = content
    )

@Composable
private fun RightArea(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxSize(),
        content = content
    )

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(start = 60.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
            .background(Color(0xff666666))
    )
}

@Composable
private fun SomeTips() =
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().offset(x = 35.dp).alpha(0.85f)
    ) {
        TipsTitle()
        Tip("이 영역에는 좌클릭/우클릭으로 최대 두 개의 아이템을 표시할 수 있습니다.")
        Tip("활성화된 효과부여 슬롯에서 나머지 비활성화된 슬롯을 수정하려면 먼저 활성화된 효과부여를 0레벨로 변경하여 비활성화합니다.")
        Tip("이미 추가된 효과, 방어구 속성을 삭제하려면 목록에서 선택된 항목을 다시 한 번 누릅니다.")
        Tip("대체로, 닫기 버튼이 없는 팝업 화면에서 빠져나가려면 주변의 빈 공간을 누르면 됩니다.")
        Tip("파일을 찾거나 저장할 때 보이는 파일 선택기에는 직접 입력하기보다는 기존 탐색기에서 경로를 복사해 붙혀넣는 것이 편리합니다.")
        Tip("수정 후 나온 결과물을 실제 게임 클라이언트가 받아줄 거라는 보장이 없으므로, 반드시 항상 백업을 만들어주세요.")
    }

@Composable
private fun TipsTitle() {
    Text(text = "아마추어 팁", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 60.dp, vertical = 30.dp))
}

@Composable
private fun Tip(text: String) {
    Text(text = text, fontSize = 24.sp, color = Color.White, modifier = Modifier.padding(horizontal = 60.dp, vertical = 20.dp))
}

@Composable
private fun ItemComparator(items: List<Item?>) {
    val scroll = rememberScrollState()
    val adapter = rememberScrollbarAdapter(scroll)
    Box(modifier = Modifier.fillMaxSize().padding(start = 75.dp)) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().verticalScroll(scroll)
        ) {
            ItemDetail(items[0])
            Spacer(modifier = Modifier.height(20.dp))
            ItemDetail(items[1])
        }
        VerticalScrollbar(
            adapter = adapter,
            style = GlobalScrollbarStyle,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 20.dp)
        )
    }
}

val GlobalScrollbarStyle = ScrollbarStyle(
    thickness = 20.dp,
    minimalHeight = 100.dp,
    hoverColor = Color.White.copy(alpha = 0.3f),
    unhoverColor = Color.White.copy(alpha = 0.15f),
    hoverDurationMillis = 0,
    shape = RoundedCornerShape(3.dp),
)
