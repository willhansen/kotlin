// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: Int>(konst i: T) {
    fun foo(s: String = "OK") = s
}

fun box() = A(42).foo()