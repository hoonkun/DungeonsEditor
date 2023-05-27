package extensions

fun <T> MutableList<T>.padEnd(minSize: Int, factory: (Int) -> T): MutableList<T> {
    if (size >= minSize) return this

    while (size < minSize) { add(factory(size)) }
    return this
}