// See KT-49659
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

// MODULE: lib
// FILE: lib.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: String>(konst konstue: T)

fun interface B {
    fun f(a: A<String>): String
}

// MODULE: main(lib)
// FILE: test.kt
fun get(b: B) = b.f(A("OK"))

fun box(): String {
    konst l = { a: A<String> -> a.konstue }
    return get(l)
}
