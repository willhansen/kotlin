// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

interface IBase

interface IQ : IBase {
    fun ok(): String
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X<T: IQ>(konst t: T): IQ {
    override fun ok(): String = t.ok()
}

interface IFoo1 {
    fun foo(): Any
}

interface IFoo2<T : IBase> {
    fun foo(): T
}

object OK : IQ {
    override fun ok(): String = "OK"
}

class Test : IFoo1, IFoo2<IQ> {
    override fun foo(): X<IQ> = X(OK)
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
    if (foo1 !is X<*>) {
        throw AssertionError("foo1 !is X: $foo1")
    }

    konst t2: IFoo2<IQ> = Test()
    konst foo2 = t2.foo()
    if (foo2 !is X<*>) {
        throw AssertionError("foo2 !is X: $foo2")
    }
    konst ok2 = foo2.ok()
    if (ok2 != "OK") {
        throw AssertionError("ok1: $ok2")
    }

    return "OK"
}