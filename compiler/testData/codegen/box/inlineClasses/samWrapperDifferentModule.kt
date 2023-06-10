// See KT-49659
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

// MODULE: lib
// FILE: lib.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst konstue: String)

fun interface B {
    fun f(a: A): String
}

// MODULE: main(lib)
// FILE: test.kt
fun get(b: B) = b.f(A("OK"))

fun box(): String {
    konst l = { a: A -> a.konstue }
    return get(l)
}
