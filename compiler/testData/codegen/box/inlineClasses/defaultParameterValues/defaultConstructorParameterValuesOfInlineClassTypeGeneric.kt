// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst z: T)

class Test(konst z: Z<Int> = Z(42)) {
    fun test() = z.z
}

fun box(): String {
    if (Test().test() != 42) throw AssertionError()
    if (Test(Z(123)).test() != 123) throw AssertionError()

    return "OK"
}