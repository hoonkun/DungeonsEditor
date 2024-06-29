package kiwi.hoonkun.utils

fun <T> List<T>.padEnd(minSize: Int, factory: (Int) -> T): List<T> {
    if (size >= minSize) return this.toList()

    val result = toMutableList()

    while (result.size < minSize) { result.add(factory(result.size)) }
    return result
}

fun <T> MutableList<T>.replace(oldElement: T, newElement: T) {
    val index = indexOf(oldElement)
    if (index < 0) return
    removeAt(index)
    add(index, newElement)
}

fun <T> List<T>.chunkedMerge(size: Int, merger: (chunked: List<T>) -> T): List<T> =
    this.chunked(size).map(merger)
