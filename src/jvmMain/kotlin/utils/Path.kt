package utils

import java.io.File

fun separatePathAndName(path: String): Pair<String, String> {
    val segments = path.split(File.separator)
    return segments.last() to replaceHomeSuffix(segments.slice(0 until segments.size - 1).joinToString(File.separator))
}

private fun replaceHomeSuffix(path: String): String {
    val home = System.getProperty("user.home")
    return "~${path.removePrefix(home)}"
}
