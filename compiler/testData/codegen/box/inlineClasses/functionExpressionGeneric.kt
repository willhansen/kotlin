// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

inline fun <T: Int> new(init: (Z<T>) -> Unit): Z<T> = Z(42) as Z<T>

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst konstue: T)

fun box(): String =
    if (new(fun(z: Z<Int>) {}).konstue == 42) "OK" else "Fail"
