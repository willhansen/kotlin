// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T) {
    @Suppress("INNER_CLASS_INSIDE_VALUE_CLASS")
    inner class Inner(konst z: Z<T>) {
        konst xx = x
    }
}

fun box(): String {
    konst zi = Z(42).Inner(Z(100))
    if (zi.xx != 42) throw AssertionError()
    if (zi.z.x != 100) throw AssertionError()

    return "OK"
}