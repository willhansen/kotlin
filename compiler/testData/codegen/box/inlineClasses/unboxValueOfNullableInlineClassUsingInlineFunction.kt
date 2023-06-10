// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

inline class X(konst i: Int)
fun unbox(x: X?): Int = checkNotNull(x).i

fun box(): String = if (unbox(X(42)) == 42) "OK" else "Fail"