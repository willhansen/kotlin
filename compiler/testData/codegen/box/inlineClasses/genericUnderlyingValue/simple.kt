// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL
// LANGUAGE: -JvmInlineValueClasses, +GenericInlineClassParameter
// IGNORE_BACKED: JVM

inline class ICAny<T>(konst konstue: T)

fun box(): String = ICAny("OK").konstue