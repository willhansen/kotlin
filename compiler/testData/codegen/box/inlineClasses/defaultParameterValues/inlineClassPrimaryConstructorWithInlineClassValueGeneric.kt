// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// IGNORE_BACKEND: JVM
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Inner<T: String>(konst result: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: Inner<String>>(konst inner: T = Inner("OK") as T)

fun box(): String {
    return A<Inner<String>>().inner.result
}
