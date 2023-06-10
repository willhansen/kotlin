// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL
// LANGUAGE: -JvmInlineValueClasses, +GenericInlineClassParameter

inline class ICIntArray<T: Int>(konst konstue: Array<T>)

fun box(): String = if (ICIntArray(arrayOf(1)).konstue[0] == 1) "OK" else "FAIL"