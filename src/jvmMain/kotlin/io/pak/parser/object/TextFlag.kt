package io.pak.parser.`object`

enum class TextFlag(val value: UInt) {
    Transient(1u shl 0),
    CultureInvariant(1u shl 1),
    ConvertedProperty(1u shl 2),
    Immutable(1u shl 3),
    InitializedFromString(1u shl 4)
}