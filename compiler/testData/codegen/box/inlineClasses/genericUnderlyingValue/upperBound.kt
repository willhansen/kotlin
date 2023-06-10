// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL
// LANGUAGE: -JvmInlineValueClasses, +GenericInlineClassParameter

inline class ICString<T: String>(konst konstue: T)

fun box(): String = ICString("OK").konstue