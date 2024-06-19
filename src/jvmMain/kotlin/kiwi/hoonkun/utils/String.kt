package kiwi.hoonkun.utils

fun String.lengthEllipsisMiddle(maxLength: Int): String {
    return if (length < maxLength) this
    else (maxLength / 2).let { remains -> "${slice(0 until remains)}...${slice(length - remains until length)}" }
}