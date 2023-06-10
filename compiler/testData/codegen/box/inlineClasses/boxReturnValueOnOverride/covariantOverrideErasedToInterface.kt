// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

interface IFoo {
    fun foo(): String
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ICFoo(konst t: IFoo): IFoo {
    override fun foo(): String = t.foo()
}

interface IBar {
    fun bar(): IFoo
}

object FooOK : IFoo {
    override fun foo(): String = "OK"
}

class Test : IBar {
    override fun bar(): ICFoo = ICFoo(FooOK)
}

fun box(): String {
    konst test: IBar = Test()
    konst bar = test.bar()
    if (bar !is ICFoo) {
        throw AssertionError("bar: $bar")
    }
    return bar.foo()
}