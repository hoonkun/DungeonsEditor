package arctic.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import arctic.ui.composables.atomic.RetroButton
import arctic.ui.composables.atomic.densityDp
import arctic.ui.composables.fonts.JetbrainsMono
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import arctic.ui.utils.getValue
import arctic.ui.utils.mutableRefOf
import arctic.ui.utils.rememberMutableInteractionSource
import arctic.ui.utils.setValue
import dungeons.Localizations
import java.io.File


private class SelectorColors {
    companion object {
        val OnBackground = Color(0xffa9b7c6)
        val Directories = Color(0xffffc660)
        val Files = Color(0xffa9b7c6)
        val FocusedCandidate = Color(0x30ffffff)
        val PathInput = Color(0xff202020)

        val IdeDocumentation = Color(0xff629755)
        val IdeFunctionProperty = Color(0xff467cda)
        val IdeGeneral = Color(0xffa9b7c6)
        val IdeKeyword = Color(0xffcc7832)
    }
}

private class SelectorFonts {

    companion object {
        val JetbrainsMono = FontFamily(
            Font (
                resource = "JetBrainsMono-Regular.ttf",
                weight = FontWeight.W400,
                style = FontStyle.Normal
            )
        )
    }

}

@Immutable
data class StateFile(
    val name: String,
    val absolutePath: String,
    val isDirectory: Boolean,
    val isFile: Boolean
)

fun File.toStateFile() = StateFile(name, absolutePath, isDirectory, isFile)

fun <T> List<T>.contentEquals(other: List<T>): Boolean {
    if (size != other.size) return false
    for ((i1, i2) in mapIndexed { index, item -> item to other[index] }) {
        if (i1 != i2) return false
    }
    return true
}

@Stable
data class StateFileList(
    val items: List<StateFile>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StateFileList

        return items.contentEquals(other.items)
    }

    override fun hashCode(): Int {
        return items.hashCode()
    }
}

@Stable
fun Collection<StateFile>.toStateFileList() = StateFileList(toList())

private val BasePath = System.getProperty("user.home")

private const val Columns = 4

private val File.typeId get() = if (isDirectory && !isFile) -1 else if (isFile) 1 else 2

private fun String.removeSuffixes(suffix: CharSequence): String {
    var result = this
    while (result.endsWith(suffix)) result = result.removeSuffix(suffix)
    return result
}

private fun StateFileList.printTargets(hintTarget: StateFile?): StateFileList {
    val range = items.indexOf(items.find { it == hintTarget }).coerceAtLeast(0)
        .div(Columns).minus(1).coerceAtLeast(0).times(Columns)
        .let {
            val minFirstRow = ((items.size + items.size.mod(Columns)).div(Columns) - 3).coerceAtLeast(0)
            val minInclusive = it.coerceAtMost(minFirstRow * Columns)
            val maxExclusive = (minInclusive + 3 * Columns).coerceAtMost(items.size)
            minInclusive until maxExclusive
        }
    return items.slice(range).toStateFileList()
}

private fun remaining(entire: StateFileList, printTargets: StateFileList): Int =
    entire.items.size - printTargets.items.size - entire.items.indexOf(printTargets.items.firstOrNull() ?: 0)

data class KeySet(
    var ctrl: Boolean,
    var shift: Boolean
)

enum class CandidateType(
    val displayName: String,
    val color: Color,
    val criteria: (StateFile) -> Boolean
) {
    Directory("directories", SelectorColors.Directories, { it.isDirectory && !it.isFile }),
    File("files", SelectorColors.Files, { !it.isDirectory })
}

val isWindows = File.separator == "\\"
val isLinux = File.separator == "/"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Selector(validator: (File) -> Boolean = { true }, selectText: String = Localizations.UiText("save"), onSelect: (File) -> Unit) {

    var useBasePath by remember { mutableStateOf(true) }

    var path by remember { mutableStateOf(TextFieldValue(File.separator, selection = TextRange(1))) }
    val entirePath by remember(path.text) { derivedStateOf { "${if (useBasePath) BasePath else ""}${path.text}" } }

    val keys = remember { KeySet(ctrl = false, shift = false) }

    var haveToShiftField by remember { mutableStateOf(false) }
    var isFieldShifting by remember { mutableRefOf(false) }

    val candidateTarget = remember(entirePath) {
        File(
            entirePath
                .removeSuffixes(".")
                .substring(0, (entirePath.lastIndexOf(File.separator) + 1))
                .let { if (it == "" && isLinux) File.separator else it }
        )
    }
    val candidates = remember(candidateTarget, entirePath) {
        (candidateTarget.listFiles() ?: arrayOf())
            .filter { file -> file.absolutePath.contains(entirePath) }
            .sortedWith(compareBy({ it.typeId }, { it.name }))
            .map { it.toStateFile() }
            .toStateFileList()
    }

    var hintTarget by remember(candidates) {
        val newState =
            if (candidates.items.size == 1) candidates.items.first().let { if (path.text.endsWith(it.name)) null else it }
            else null

        if (newState != null) {
            haveToShiftField = true
            isFieldShifting = true
        }

        mutableStateOf(newState)
    }
    val hint = remember(path.text, hintTarget) {
        val target = hintTarget ?: return@remember ""

        val entered = path.text.let { it.substring(it.lastIndexOf(File.separator) + 1, it.length) }
        val entire = target.name

        entire.substring(entered.length until entire.length)
    }

    val value = remember(path.text, path.selection, haveToShiftField, hint) {
        val newText = "${path.text}${hint}"
        val annotatedText = AnnotatedString(
            text = newText,
            listOf(AnnotatedString.Range(
                item = SpanStyle(color = SelectorColors.OnBackground.copy(alpha = 0.313f)),
                start = path.text.length,
                end = newText.length
            ))
        )
        val selection =
            if (haveToShiftField) TextRange(path.selection.start + hint.length)
            else path.selection

        if (isFieldShifting && !haveToShiftField)
            isFieldShifting = false

        TextFieldValue(annotatedText, selection)
    }

    val selected = remember(path.text, useBasePath) {
        File("${if (useBasePath) BasePath else ""}${path.text}").takeIf(validator)
    }

    val requester = remember { FocusRequester() }

    val value2path: (TextFieldValue) -> TextFieldValue = transform@ {
        var newPath =
            if (isFieldShifting) it.text.slice(0 until it.text.length - 1)
            else it.text
        newPath = newPath.removeSuffix(hint).replace("//", "/")
        if (isFieldShifting) newPath += it.text.last()

        val pasted = newPath.length - path.text.length > 2
        if (pasted && newPath.startsWith(BasePath) && useBasePath) {
            useBasePath = false
        }
        if (pasted && newPath.startsWith("\\$BasePath") && useBasePath) {
            newPath = newPath.replace("\\$BasePath", "")
        }

        TextFieldValue(
            text = newPath,
            selection = TextRange(
                it.selection.start.coerceAtMost(newPath.length),
                it.selection.end.coerceAtMost(newPath.length)
            )
        )
    }

    val complete = complete@ {
        val target = hintTarget ?: return@complete false
        val newPath = "${path.text}$hint${if (target.isDirectory) File.separator  else ""}"
        path = TextFieldValue(text = newPath, selection = TextRange(newPath.length))
        hintTarget = null
        true
    }

    val onKeyEvent: (KeyEvent) -> Boolean = {
        if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
            if (hintTarget != null && candidates.items.size == 1) {
                if (!keys.shift) complete()
            } else if (candidates.items.isNotEmpty()) {
                val noneSelected = hintTarget == null
                val lastFile = hintTarget?.absolutePath == candidates.items.last().absolutePath
                val firstFile = hintTarget?.absolutePath == candidates.items.first().absolutePath
                hintTarget =
                    if (!keys.shift) {
                        if (noneSelected || lastFile) candidates.items.first()
                        else candidates.items[candidates.items.indexOf(hintTarget) + 1]
                    } else {
                        if (noneSelected || firstFile) candidates.items.last()
                        else candidates.items[candidates.items.indexOf(hintTarget) - 1]
                    }
                haveToShiftField = true
                isFieldShifting = true
            }
            requester.requestFocus()
            true
        } else if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
            if (!complete() && selected != null && keys.ctrl) {
                onSelect(selected)
                true
            } else {
                false
            }
        } else if (it.key == Key.CtrlLeft || it.key == Key.CtrlRight) {
            keys.ctrl = it.type == KeyEventType.KeyDown
            true
        } else if (it.key == Key.ShiftLeft || it.key == Key.ShiftRight) {
            keys.shift = it.type == KeyEventType.KeyDown
            true
        } else {
            false
        }
    }

    SideEffect {
        requester.requestFocus()
        if (haveToShiftField) haveToShiftField = false
    }

    SelectorRoot {
        Padded {
            BasePathDocumentation(text = "/** you can use '..' to go parent directory */")
            Row {
                BasePathToggleProperty(key = "useBasePath", value = if (useBasePath) "true" else "false") {
                    if (it && isWindows && path.text.isEmpty()) path = path.copy(text = "\\")
                    else if (isWindows && path.text == File.separator) path = path.copy(text = "")
                    useBasePath = it
                }
                Spacer(modifier = Modifier.width(30.dp))
                BasePathProperty(key = "basePath", value = BasePath, disabled = !useBasePath)
            }
        }
        PathInputBox {
            PathInput(
                value = value,
                onValueChange = { if (it.text.isNotEmpty() || (isWindows && !useBasePath)) path = value2path(it) },
                onKeyEvent = onKeyEvent,
                hideCursor = haveToShiftField,
                focusRequester = requester
            )
            Select(enabled = selected != null, text = selectText) { selected?.let { onSelect(it) } }
        }
        Padded {
            if (!candidateTarget.isDirectory) return@Padded

            Candidates(
                type = CandidateType.Directory,
                candidates = candidates,
                hintTarget = hintTarget
            )

            Candidates(
                type = CandidateType.File,
                candidates = candidates,
                hintTarget = hintTarget
            )
        }
    }

}

@Composable
fun Candidates(
    type: CandidateType,
    candidates: StateFileList,
    hintTarget: StateFile?
) {
    val typed = remember(candidates, type.criteria) { candidates.items.filter(type.criteria).toStateFileList() }
    val printTargets = remember(typed, hintTarget) { typed.printTargets(hintTarget) }
    val remaining = remember(typed, printTargets) { remaining(typed, printTargets) }

    if (printTargets.items.isEmpty()) return

    for (chunked in printTargets.items.chunked(Columns)) {
        Row {
            for (candidate in chunked) {
                Candidate(type, candidate, candidate == hintTarget && printTargets.items.size != 1)
            }
            if (chunked.size == 3) {
                Spacer(modifier = Modifier.weight(1f).padding(end = 30.dp, bottom = 10.dp))
            }
        }
    }
    if (remaining > 0) Remaining(remaining, type)

    Spacer(modifier = Modifier.height(30.dp))
}

@Composable
fun RowScope.Candidate(
    type: CandidateType,
    candidate: StateFile,
    focused: Boolean
) = Box(modifier = Modifier.weight(1f).padding(end = 30.dp, bottom = 10.dp)) {
    Text(
        text = candidate.name,
        color = type.color,
        fontSize = 20.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.drawBehind { if (focused) drawRect(SelectorColors.FocusedCandidate) }
    )
}

@Composable
fun Remaining(
    count: Int,
    type: CandidateType
) = Text(
    "...$count ${type.displayName} more",
    color = type.color,
    fontSize = 20.sp,
    maxLines = 1
)

@Composable
fun SelectorRoot(
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .requiredWidthIn(min = 1000.dp)
            .padding(start = 25.dp, end = 25.dp, top = 32.dp)
    ) {
        Column(
            content = content
        )
    }
}

@Composable
fun Padded(
    top: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) = Column(modifier = Modifier.padding(start = 30.dp, end = 30.dp, top = top), content = content)

@Composable
fun BasePathDocumentation(text: String) =
    Text(
        text = text,
        color = SelectorColors.IdeDocumentation,
        fontFamily = SelectorFonts.JetbrainsMono,
        fontSize = 20.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )

@Composable
fun BasePathProperty(key: String, value: String, disabled: Boolean) =
    Text(
        AnnotatedString(
            text = "$key = $value",
            spanStyles = listOf(
                AnnotatedString.Range(
                    item = SpanStyle(color = SelectorColors.IdeFunctionProperty),
                    start = 0,
                    end = key.length + 2
                )
            )
        ),
        color = SelectorColors.IdeGeneral,
        fontSize = 24.sp,
        fontFamily = JetbrainsMono,
        modifier = Modifier.padding(bottom = 2.dp).alpha(if (disabled) 0.35f else 1f)
    )

@Composable
fun BasePathToggleProperty(key: String, value: String, onClick: (Boolean) -> Unit) =
    Text(
        AnnotatedString(
            text = "$key = ${value.padEnd(5, ' ')}",
            spanStyles = listOf(
                AnnotatedString.Range(
                    item = SpanStyle(color = SelectorColors.IdeFunctionProperty),
                    start = 0,
                    end = key.length + 2
                )
            )
        ),
        color = SelectorColors.IdeKeyword,
        fontSize = 24.sp,
        fontFamily = JetbrainsMono,
        modifier = Modifier
            .padding(bottom = 2.dp)
            .clickable(rememberMutableInteractionSource(), null) { onClick(value == "false") }
    )

@Composable
fun PathInputBox(content: @Composable RowScope.() -> Unit) =
    Box(
        modifier = Modifier.padding(vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .drawBehind {
                    drawRect(SelectorColors.PathInput, topLeft = Offset(0f, densityDp(7)), size = Size(size.width, size.height - densityDp(14)))
                    drawRect(SelectorColors.PathInput, topLeft = Offset(densityDp(7), 0f), size = Size(size.width - densityDp(14), size.height))
                },
            content = content
        )
    }

@Composable
fun RowScope.PathInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onKeyEvent: (KeyEvent) -> Boolean,
    hideCursor: Boolean,
    focusRequester: FocusRequester
) = BasicTextField(
    value = value,
    onValueChange = onValueChange,
    textStyle = TextStyle(
        color = SelectorColors.IdeGeneral,
        fontSize = 24.sp,
        fontFamily = SelectorFonts.JetbrainsMono
    ),
    cursorBrush =
        if (hideCursor) SolidColor(Color.Transparent)
        else SolidColor(SelectorColors.IdeGeneral),
    singleLine = true,
    modifier = Modifier.weight(1f)
        .onKeyEvent { onKeyEvent(it) }
        .focusRequester(focusRequester)
        .padding(start = 20.dp, end = 20.dp)
)

@Composable
fun Select(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.padding(5.dp)) {
        RetroButton(text, Color(0xff3f8e4f), "outline", enabled = enabled, buttonSize = 115.dp to 70.dp, onClick = onClick)
    }
}
