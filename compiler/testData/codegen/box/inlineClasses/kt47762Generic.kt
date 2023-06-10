// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: Int>(konst a: T = 1 as T) {
    companion object {
        konst a: Int = 2
    }
}

fun box(): String {
    if (A.a != 2) return "FAIL1"
    konst instance = A<Int>()
    return if (instance.a != 1) "FAIL2" else "OK"
}
