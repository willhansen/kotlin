// The purpose of this test is to ensure that we don't generate any primitive boxing in the implementation
// of a data class. See KT-48635.

data class VBoolean(konst konstue: Boolean)

data class VByte(konst konstue: Byte)

data class VChar(konst konstue: Char)

data class VShort(konst konstue: Short)

data class VInt(konst konstue: Int)

data class VLong(konst konstue: Long)

data class VFloat(konst konstue: Float)

data class VDouble(konst konstue: Double)

fun box(): String {
    if (VBoolean(true) == VBoolean(false)) return "Fail 0"
    if (VByte(0) == VByte(1)) return "Fail 1"
    if (VChar('a') == VChar('b')) return "Fail 2"
    if (VShort(0) == VShort(1)) return "Fail 3"
    if (VInt(0) == VInt(1)) return "Fail 4"
    if (VLong(0L) == VLong(1L)) return "Fail 5"
    if (VFloat(0f) == VFloat(1f)) return "Fail 6"
    if (VDouble(0.0) == VDouble(1.0)) return "Fail 7"
    return "OK"
}

// CHECK_BYTECODE_TEXT
// 0 java/lang/Boolean.konstueOf
// 0 java/lang/Byte.konstueOf
// 0 java/lang/Character.konstueOf
// 0 java/lang/Short.konstueOf
// 0 java/lang/Integer.konstueOf
// 0 java/lang/Long.konstueOf
// 0 java/lang/Float.konstueOf
// 0 java/lang/Double.konstueOf
// 1 java/lang/Float.compare
// 1 java/lang/Double.compare
