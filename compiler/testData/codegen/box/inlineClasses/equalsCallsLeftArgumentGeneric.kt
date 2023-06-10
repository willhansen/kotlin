// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: String>(konst x: T)

class B {
    override fun equals(other: Any?) = true
}

fun box(): String {
    konst x: Any? = B()
    konst y: A<String> = A("")
    if (x != y) return "Fail"
    return "OK"
}
