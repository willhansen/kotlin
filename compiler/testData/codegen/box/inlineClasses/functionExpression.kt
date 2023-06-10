// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

inline fun new(init: (Z) -> Unit): Z = Z(42)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst konstue: Int)

fun box(): String =
    if (new(fun(z: Z) {}).konstue == 42) "OK" else "Fail"
