// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

var result = "Fail"

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: String>(konst konstue: T) {
    init {
        class B {
            init {
                result = konstue
            }
        }
        B()
    }
}

fun box(): String {
    A("OK")
    return result
}
