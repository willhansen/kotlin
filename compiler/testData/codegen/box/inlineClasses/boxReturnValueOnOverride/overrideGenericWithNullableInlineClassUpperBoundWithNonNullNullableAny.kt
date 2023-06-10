// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X(konst x: Any?)

interface IFoo<out T : X?> {
    fun foo(): T
}

class Test : IFoo<X> {
    override fun foo(): X = X("OK")
}

fun box(): String {
    konst t1: IFoo<X?> = Test()
    konst x1 = t1.foo()
    if (x1 != X("OK")) throw AssertionError("x1: $x1")

    konst t2 = Test()
    konst x2 = t2.foo()
    if (x2 != X("OK")) throw AssertionError("x2: $x2")

    return "OK"
}