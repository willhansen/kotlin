// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

interface IFoo1<out T> {
    fun foo(): T
}

interface IFoo2<out T> {
    fun foo(): T
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X(konst x: String)

class Test : IFoo1<X>, IFoo2<X> {
    override fun foo(): X = X("OK")
}

fun box(): String {
    konst t1: IFoo1<Any> = Test()
    konst foo1 = t1.foo()
    if (foo1 !is X) {
        throw AssertionError("foo1 !is X: $foo1")
    }
    if (foo1.x != "OK") {
        throw AssertionError("foo1.x != 'OK': $foo1")
    }

    konst t2: IFoo2<Any> = Test()
    konst foo2 = t2.foo()
    if (foo2 !is X) {
        throw AssertionError("foo2 !is X: $foo2")
    }
    if (foo2.x != "OK") {
        throw AssertionError("foo2.x != 'OK': $foo2")
    }

    return "OK"
}