package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.FileSelector
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.io.DungeonsJsonFile

@Composable
fun FileSaveOverlay(
    editor: EditorState,
    postSave: () -> Unit = { },
    requestClose: () -> Unit
) {
    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("file_save_title"),
            description = Localizations.UiText("file_save_description")
        )
        // TODO: 원래 파일의 경로를 표시할 것
        //  취소하고 닫기? 같은 버튼도 있으면 좋지 않을까.
        //  백업 파일 생성 여부 관련 체크박스도 만들 것.
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.size(1050.dp, 640.dp)) {
            FileSelector(
                onSelect = {
                    editor.stored.save(DungeonsJsonFile(it))
                    requestClose()
                    postSave()
                }
            )
        }
    }
}