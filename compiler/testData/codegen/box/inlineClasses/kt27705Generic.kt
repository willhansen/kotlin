// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T) {
    @Suppress("INNER_CLASS_INSIDE_VALUE_CLASS")
    inner class Inner(konst y: Int) {
        konst xx = x
    }
}

fun box(): String {
    konst zi = Z(42).Inner(100)
    if (zi.xx != 42) throw AssertionError()
    if (zi.y != 100) throw AssertionError()

    return "OK"
}