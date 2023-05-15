package io.pak.parser.`object`

enum class PakVersion(val value: Int) {
    Initial(1),
    NoTimestamps(2),
    CompressionEncryption(3),
    IndexEncryption(4),
    RelativeChunkOffsets(5),
    DeleteRecords(6),
    KeyGuidEncryption(7),
    FNameBasedCompressionMethod(8),
    FrozenIndex(9),
    PathHashIndex(10),

    Latest(PakVersion.values().lastIndex - 2),
    Last(PakVersion.values().lastIndex - 1),
    Invalid(PakVersion.values().lastIndex),
}