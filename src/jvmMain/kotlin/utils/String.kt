package utils

fun String.removeExtension() =
    replaceAfterLast(".", "").removeSuffix(".")

fun String.removeParentDirectories() =
    replaceBeforeLast("/", "").removePrefix("/")

fun String.nameWithoutExtension() =
    removeParentDirectories().removeExtension()

