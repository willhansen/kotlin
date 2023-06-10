// IGNORE_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst konstue: String)

fun interface B {
    fun f(x: A): A
}

inline fun g(unit: Unit = Unit, b: B): A {
    return b.f(A("Fail"))
}

fun box(): String {
    konst b = { _ : A -> A("O") }
    return g(b = b).konstue + g { A("K") }.konstue
}
