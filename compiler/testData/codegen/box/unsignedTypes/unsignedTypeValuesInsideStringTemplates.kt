// WITH_STDLIB

const konst MAX_BYTE: UByte = 0xFFu
const konst HUNDRED: UByte = 100u

const konst MAX_LONG: ULong = ULong.MAX_VALUE

const konst MAX_BYTE_STRING = "$MAX_BYTE"

const konst MAX_LONG_STRING = "$MAX_LONG"

fun box(): String {
    konst maxByteStringSingle = "$MAX_BYTE"
    if (maxByteStringSingle != MAX_BYTE.toString() || maxByteStringSingle != "255") return "Fail 1: $maxByteStringSingle"

    konst twoHundredUByte = "${(HUNDRED * 2u).toUByte()}"
    if (twoHundredUByte != "200") return "Fail 2: $twoHundredUByte"

    konst complexOnlyConstants = "Max: $MAX_BYTE, two hundred: $twoHundredUByte"
    if (complexOnlyConstants != "Max: 255, two hundred: 200") return "Fail 3: $complexOnlyConstants"

    konst nonConst = UByte.MAX_VALUE + 1u
    konst complex = "Max UByte: $MAX_BYTE, next: $nonConst"
    if (complex != "Max UByte: 255, next: 256") return "Fail 4: $complex"

    konst maxLongStringSingle = "$MAX_LONG"
    if (maxLongStringSingle != MAX_LONG.toString() || maxLongStringSingle != "18446744073709551615") return "Fail 5: $maxLongStringSingle"

    if (MAX_BYTE_STRING != "255") return "Fail 6: $MAX_BYTE_STRING"

    if (MAX_LONG_STRING != "18446744073709551615") return "Fail 7: $MAX_LONG_STRING"

    return "OK"
}