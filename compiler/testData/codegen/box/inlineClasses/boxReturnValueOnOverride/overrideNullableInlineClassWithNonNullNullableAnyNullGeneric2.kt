// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X<T: Any>(konst x: T?)

interface IFoo {
    fun foo(): X<String>?
}

class Test : IFoo {
    override fun foo(): X<String>? = X(null)
}

fun box(): String {
    konst t1: IFoo = Test()
    konst x1 = t1.foo()
    if (x1 != X(null)) throw AssertionError("x1: $x1")

    konst t2 = Test()
    konst x2 = t2.foo()
    if (x2 != X(null)) throw AssertionError("x2: $x2")

    return "OK"
}