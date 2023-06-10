// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X(konst s: String)
fun foo(x: X, block: (X) -> String = { it.s }) = block(x)

fun box(): String {
    return foo(X("OK"))
}
