// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

var result = "Fail"

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst konstue: String)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class B(konst a: A) {
    init {
        result = a.konstue
    }
}

fun box(): String {
    B(A("OK"))
    return result
}
