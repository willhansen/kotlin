// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst x: String)

class B {
    override fun equals(other: Any?) = true
}

fun box(): String {
    konst x: Any? = B()
    konst y: A = A("")
    if (x != y) return "Fail"
    return "OK"
}
