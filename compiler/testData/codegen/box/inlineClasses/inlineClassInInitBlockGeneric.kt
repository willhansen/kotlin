// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

var result = "Fail"

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: String>(konst konstue: T) {
    fun f() = konstue + "K"
}

class B<T: String>(konst a: A<T>) {
    konst result: String
    init {
        result = a.f()
    }
}

fun box(): String {
    return B(A("O")).result
}
