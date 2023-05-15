package io.pak.parser.`object`

enum class BulkDataFlags(val value: UInt) {
    None(0u),
    PayloadAtEndOfFile(1u shl 0),
    SerializeCompressedZLIB(1u shl 1),
    ForceSingleElementSerialization(1u shl 2),
    SingleUse(1u shl 3),
    Unused(1u shl 5),
    ForceInlinePayload(1u shl 6),
    SerializeCompressed(SerializeCompressedZLIB.value),
    ForceStreamPayload(1u shl 7),
    PayloadInSeparateFile(1u shl 8),
    SerializeCompressedBitWindow(1u shl 9),
    ForceNotInlinePayload(1u shl 10),
    OptionalPayload(1u shl 11),
    MemoryMappedPayload(1u shl 12),
    Size64Bit(1u shl 13),
    DuplicateNonOptionalPayload(1u shl 14)
}