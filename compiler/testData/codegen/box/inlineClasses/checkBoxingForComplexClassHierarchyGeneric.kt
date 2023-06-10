// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC<T: Int>(konst x: T)

interface I<T> {
    fun foo(t: T): T
}

interface I2: I<IC<Int>>

open class A<T> {
    fun foo(t: T): T =
        if (t is IC<*>)
            IC(20 + t.x) as T
        else
            t
}

open class B: A<IC<Int>>()
class C: I2, B()

fun box(): String {
    konst ic = IC(10)
    konst i: I<IC<Int>> = C()
    konst i2: I2 = C()
    konst a: A<IC<Int>> = C()
    konst b: B = C()
    konst c: C = C()

    konst fooI = i.foo(ic).x
    if (fooI != 30) return "Fail I"

    // Test calling abstract fake override methods
    // with signature specialized by inline class
    konst fooI2 = i2.foo(ic).x
    if (fooI2 != 30) return "Fail I2"

    konst fooA = a.foo(ic).x
    if (fooA != 30) return "Fail A"

    konst fooB = b.foo(ic).x
    if (fooB != 30) return "Fail B"

    konst resC = c.foo(ic).x
    if (resC != 30) return "Fail C"

    return "OK"
}
