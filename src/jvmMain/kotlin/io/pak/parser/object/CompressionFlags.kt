package io.pak.parser.`object`

enum class CompressionFlags(val value: UInt) {
    // Formats
    None(0x00u),
    ZLIB(0x01u),
    GZIP(0x02u),
    Custom(0x04u),
    @Deprecated("marked as deprecated in original source") FormatFlagsMask(0xFu),

    // Options
    NoFlags(0x00u),
    MemoryBiased(0x10u),
    SpeedBiased(0x20u),
    Padded(0x80u),
    OptionFlagsMask(0xF0u)
}