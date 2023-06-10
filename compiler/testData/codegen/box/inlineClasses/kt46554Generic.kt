// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

var result = "Fail"

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: String>(konst konstue: T) {
    constructor() : this("OK" as T)

    init {
        result = konstue
    }
}

fun box(): String {
    A<String>()
    return result
}
