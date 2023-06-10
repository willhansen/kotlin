// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Inner(konst result: String)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst inner: Inner = Inner("OK"))

fun box(): String {
    return A().inner.result
}
