// IGNORE_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: String>(konst konstue: T)

fun interface B {
    fun f(x: A<String>): A<String>
}

inline fun g(unit: Unit = Unit, b: B): A<String> {
    return b.f(A("Fail"))
}

fun box(): String {
    konst b = { _ : A<*> -> A("O") }
    return g(b = b).konstue + g { A("K") }.konstue
}
