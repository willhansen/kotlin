// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X<T: Any>(konst x: T?)

interface IBar {
    fun bar(): Any
}

interface IFoo : IBar {
    fun foo(): Any
    override fun bar(): X<String>
}

class TestX : IFoo {
    override fun foo(): X<String> = X("O")
    override fun bar(): X<String> = X("K")
}

fun box(): String {
    konst t: IFoo = TestX()
    konst tFoo = t.foo()
    if (tFoo !is X<*>) {
        throw AssertionError("X expected: $tFoo")
    }

    konst t2: IBar = TestX()
    konst tBar = t.bar()
    if (tBar !is X<*>) {
        throw AssertionError("X expected: $tBar")
    }

    return (t.foo() as X<String>).x!!.toString() + (t2.bar() as X<String>).x!!.toString()
}