package kiwi.hoonkun.utils

fun <T> List<T>.padEnd(minSize: Int, factory: (Int) -> T): List<T> {
    if (size >= minSize) return this

    val result = toMutableList()

    while (result.size < minSize) { result.add(factory(result.size)) }
    return result
}

fun <T> MutableList<T>.replace(from: T, into: T) {
    val index = indexOf(from)
    if (index < 0) return
    removeAt(index)
    add(index, into)
}
