// !DIAGNOSTICS: -UNUSED_PARAMETER

fun ubyte(vararg a: UByte) {}
fun ushort(vararg a: UShort) {}
fun uint(vararg a: UInt) {}
fun ulong(vararg a: ULong) {}

class ValueParam(vararg konst a: ULong)

annotation class Ann(vararg konst a: UInt)

fun array(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> a: UIntArray) {}
