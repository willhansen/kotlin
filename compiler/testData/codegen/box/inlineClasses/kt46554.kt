// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

var result = "Fail"

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst konstue: String) {
    constructor() : this("OK")

    init {
        result = konstue
    }
}

fun box(): String {
    A()
    return result
}
