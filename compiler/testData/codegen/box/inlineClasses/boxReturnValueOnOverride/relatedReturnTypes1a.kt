// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

interface IQ {
    fun ok(): String
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X(konst t: IQ): IQ {
    override fun ok(): String = t.ok()
}

interface IFoo1 {
    fun foo(): Any
}

interface IFoo2 {
    fun foo(): IQ
}

object OK : IQ {
    override fun ok(): String = "OK"
}

class Test : IFoo1, IFoo2 {
    override fun foo(): X = X(OK)
}

fun box(): String {
    konst t1: IFoo1 = Test()
    konst foo1 = t1.foo()
    if (foo1 !is IQ) {
        throw AssertionError("foo1 !is IQ: $foo1")
    }
    konst ok1 = foo1.ok()
    if (ok1 != "OK") {
        throw AssertionError("ok1: $ok1")
    }
    if (foo1 !is X) {
        throw AssertionError("foo1 !is X: $foo1")
    }

    konst t2: IFoo2 = Test()
    konst foo2 = t2.foo()
    if (foo2 !is X) {
        throw AssertionError("foo2 !is X: $foo2")
    }
    konst ok2 = foo2.ok()
    if (ok2 != "OK") {
        throw AssertionError("ok1: $ok2")
    }

    return "OK"
}