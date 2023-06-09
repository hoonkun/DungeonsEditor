package extensions

fun <T> MutableList<T>.padEnd(minSize: Int, factory: (Int) -> T): MutableList<T> {
    if (size >= minSize) return this

    while (size < minSize) { add(factory(size)) }
    return this
}

fun <T> List<T>.padEndRemaining(multiplier: Int, factory: (Int) -> T) = this.toMutableList().padEnd(size + (multiplier - size % multiplier), factory)

fun <T> MutableList<T>.replace(from: T, into: T) {
    val index = indexOf(from)
    if (index < 0) return
    removeAt(index)
    add(index, into)
}
