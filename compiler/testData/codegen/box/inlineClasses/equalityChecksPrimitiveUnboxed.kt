// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

// The purpose of this test is to ensure that we don't generate any primitive boxing in the implementation
// of a @JvmInline konstue class. See KT-48635.

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class VBoolean(konst konstue: Boolean)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class VByte(konst konstue: Byte)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class VChar(konst konstue: Char)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class VShort(konst konstue: Short)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class VInt(konst konstue: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class VLong(konst konstue: Long)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class VFloat(konst konstue: Float)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class VDouble(konst konstue: Double)

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
// 2 java/lang/Float.compare
// 2 java/lang/Double.compare
