package io.pak.parser.`object`

enum class TextHistoryType(val value: Byte) {
    None(-1),
    Base(0),
    NamedFormat(1),
    OrderedFormat(2),
    ArgumentFormat(3),
    AsNumber(4),
    AsPercent(5),
    AsCurrency(6),
    AsDate(7),
    AsTime(8),
    AsDateTime(9),
    Transform(10),
    StringTableEntry(11),
    TextGenerator(12)
}