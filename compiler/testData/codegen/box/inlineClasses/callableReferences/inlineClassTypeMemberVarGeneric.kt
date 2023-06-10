// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T)

class C(var z: Z<Int>)

fun box(): String {
    konst ref = C::z

    konst x = C(Z(42))

    if (ref.get(x).x != 42) throw AssertionError()

    ref.set(x, Z(1234))
    if (ref.get(x).x != 1234) throw AssertionError()

    return "OK"
}