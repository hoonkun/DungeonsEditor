package kiwi.hoonkun.ui.composables.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp


@Composable
fun ErrorOverlay(
    e: Exception,
    title: String,
    description: String? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                OverlayTitleText(title)
                if (description != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OverlayDescriptionText(description)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = Localizations["error_detail_label"],
                    style = LocalTextStyle.current.copy(fontSize = 22.sp, color = Color.White.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            Spacer(
                modifier = Modifier
                    .padding(horizontal = 64.dp)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.25f))
            )
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(2f)
            ) {
                Text(
                    text = buildExceptionNameString(e),
                    style = LocalTextStyle.current.copy(
                        color = Color(0xffF75464),
                        fontSize = 24.sp
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                for (stackElement in e.stackTrace.slice(0 until 10.coerceAtMost(e.stackTrace.size))) {
                    Text(
                        text = buildStackElementString(stackElement),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                if (e.stackTrace.size > 10) {
                    Text(
                        text = "${e.stackTrace.size - 10} lines collapsed",
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            }
        }
    }
}

private data class ParsedClassName(
    val packageName: String,
    val mainClassName: String,
    val nestedClasses: List<String>
)

val FunctionStyle = SpanStyle(color = Color(0xff56A8F5))
val KeywordStyle = SpanStyle(color = Color(0xffCF8E6D))
val DocumentationStyle = SpanStyle(color = Color(0xff5F826B))
val NotImportantStyle = SpanStyle(color = Color.White.copy(alpha = 0.5f))
val DelimiterStyle = SpanStyle(color = Color.White.copy(alpha = 0.25f))

private fun buildExceptionNameString(e: Exception): AnnotatedString =
    buildAnnotatedString {
        append(e::class.qualifiedName)
        e.cause?.let { append(" <- ${it::class.qualifiedName}") }

        append(": ${e.message}")
    }

private fun buildStackElementString(element: StackTraceElement): AnnotatedString =
    buildAnnotatedString {
        val (packageName, mainClassName, nestedClasses) = parseClassName(element.className)

        withStyle(NotImportantStyle) { appendLine(packageName) }

        append(mainClassName)
        if (nestedClasses.isNotEmpty()) append(".${nestedClasses.joinToString(".")}")

        append("  ")

        if (element.isNativeMethod) withStyle(KeywordStyle) { append("native ") }
        withStyle(FunctionStyle) {
            val segments = element.methodName.split('$')
            segments.forEachIndexed { index, string ->
                append(string)
                if (index != segments.size - 1) {
                    withStyle(DelimiterStyle) {
                        append("$")
                    }
                }
            }
        }

        append("  ")

        if (element.fileName != null && element.lineNumber >= 0) {
            withStyle(DocumentationStyle) { append("/** ${element.fileName} ${element.lineNumber} */") }
        }
    }

private fun parseClassName(rawClassName: String): ParsedClassName {
    val d1 = rawClassName.lastIndexOf('.')
    val packageName = rawClassName.slice(0 until d1)

    val classNameSegment = rawClassName.slice((d1 + 1) until rawClassName.length)
    val d2 = classNameSegment.indexOf('$')

    if (d2 < 0) return ParsedClassName(packageName, classNameSegment, emptyList())

    val mainClassName = classNameSegment.slice(0 until d2)
    val nestedClasses = classNameSegment.slice((d2 + 1) until classNameSegment.length).split('$')

    return ParsedClassName(packageName, mainClassName, nestedClasses)
}
