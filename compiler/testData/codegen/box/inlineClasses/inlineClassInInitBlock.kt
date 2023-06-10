// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

var result = "Fail"

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst konstue: String) {
    fun f() = konstue + "K"
}

class B(konst a: A) {
    konst result: String
    init {
        result = a.f()
    }
}

fun box(): String {
    return B(A("O")).result
}
