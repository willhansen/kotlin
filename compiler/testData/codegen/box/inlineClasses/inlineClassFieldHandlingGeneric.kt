// WITH_STDLIB
// IGNORE_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

var result = "Fail"

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: String>(konst konstue: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class B<T: A<String>>(konst a: T) {
    init {
        result = a.konstue
    }
}

fun box(): String {
    B(A("OK"))
    return result
}
