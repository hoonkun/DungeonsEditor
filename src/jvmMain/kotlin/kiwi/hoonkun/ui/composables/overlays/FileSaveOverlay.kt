package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.BasePathToggleProperty
import kiwi.hoonkun.ui.composables.base.FileSelector
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.io.DungeonsJsonFile
import java.io.File

@Composable
fun FileSaveOverlay(
    editor: EditorState,
    postSave: () -> Unit = { },
    requestClose: () -> Unit
) {
    val overlays = LocalOverlayState.current
    var createBackup by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        OverlayDescriptionText(Localizations.UiText("tap_to_cancel"), fontSize = 18.sp)
    }

    ContentRoot {
        OverlayTitleDescription(
            title = Localizations.UiText("file_save_title"),
            description = Localizations.UiText("file_save_description")
        )
        Spacer(modifier = Modifier.height(20.dp))
        FileSelector(
            defaultCandidate = { File(editor.stored.sourcePath) },
            modifier = Modifier
                .size(1050.dp, 640.dp)
                .padding(start = 25.dp, end = 25.dp, top = 32.dp),
            options = {
                BasePathToggleProperty(
                    key = "createBackup",
                    value = if (createBackup) "true" else "false"
                ) {
                    createBackup = it
                }
                Spacer(modifier = Modifier.width(30.dp))
            },
            onSelect = {
                try {
                    editor.stored.save(DungeonsJsonFile(it), createBackup)
                    requestClose()
                    postSave()
                } catch (e: Exception) {
                    overlays.make {
                        ErrorOverlay(
                            e = e,
                            title = Localizations["error_save"],
                            description = Localizations["error_save_description"]
                        )
                    }
                }
            }
        )
    }
}