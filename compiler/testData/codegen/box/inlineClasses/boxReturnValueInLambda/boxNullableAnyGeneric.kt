// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X<T>(konst x: T)

fun useX(x: X<String?>): String = x.x!!

fun <T> call(fn: () -> T) = fn()

fun box() = useX(call { X("OK") })