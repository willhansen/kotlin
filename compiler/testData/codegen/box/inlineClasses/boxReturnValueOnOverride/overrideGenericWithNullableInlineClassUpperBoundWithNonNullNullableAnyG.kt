// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X<T>(konst x: T)

interface IFoo<out T : X<String>?> {
    fun foo(): T
}

class Test : IFoo<X<String>> {
    override fun foo(): X<String> = X("OK")
}

fun box(): String {
    konst t1: IFoo<X<String>> = Test()
    konst x1 = t1.foo()
    if (x1 != X("OK")) throw AssertionError("x1: $x1")

    konst t2 = Test()
    konst x2 = t2.foo()
    if (x2 != X("OK")) throw AssertionError("x2: $x2")

    return "OK"
}