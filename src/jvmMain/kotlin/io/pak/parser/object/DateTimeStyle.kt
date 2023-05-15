package io.pak.parser.`object`

enum class DateTimeStyle(val value: Byte) {
    Default(0x00),
    Short(0x01),
    Medium(0x02),
    Long(0x03),
    Full(0x04)
}