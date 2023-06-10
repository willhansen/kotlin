// WITH_STDLIB
// IGNORE_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: Int>(konst x: T) {
    fun f(): Int = super.hashCode()
}

fun box(): String {
    konst a = A(1).f()
    return "OK"
}
