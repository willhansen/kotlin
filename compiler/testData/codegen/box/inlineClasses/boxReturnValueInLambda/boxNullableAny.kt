// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X(konst x: Any?)

fun useX(x: X): String = x.x as String

fun <T> call(fn: () -> T) = fn()

fun box() = useX(call { X("OK") })