// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

interface IFoo {
    fun foo(): String
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC(konst x: String) : IFoo {
    private fun privateFun() = x
    override fun foo() = privateFun()
}

fun box(): String {
    konst x: IFoo = IC("OK")
    return x.foo()
}