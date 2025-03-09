package kiwi.hoonkun.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kiwi.hoonkun.app.editor.dungeons.dungeonseditor.generated.resources.JetBrainsMono_Regular
import kiwi.hoonkun.app.editor.dungeons.dungeonseditor.generated.resources.Res
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font


@Composable
internal fun Res.font.JetbrainsMono() =
    FontFamily(Font(resource = JetBrainsMono_Regular, weight = FontWeight.W400, style = FontStyle.Normal))

@OptIn(ExperimentalResourceApi::class)
internal fun Res.bytesOf(path: String): String = runBlocking { String(readBytes("files/$path")) }