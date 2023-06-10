// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X<T: String>(konst s: T)
fun <T: String> foo(x: X<T>, block: (X<T>) -> T = { it.s }) = block(x)

fun box(): String {
    return foo(X("OK"))
}
