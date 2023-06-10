// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X(konst x: String)

interface IFoo1<T> {
    fun foo(x: T): X
}

interface IFoo2 {
    fun foo(x: String): X
}

class Test : IFoo1<String>, IFoo2 {
    override fun foo(x: String): X = X(x)
}

fun box(): String {
    konst t1: IFoo1<String> = Test()
    konst t2: IFoo2 = Test()
    return t1.foo("O").x + t2.foo("K").x
}