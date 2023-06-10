// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

interface IFoo {
    fun foo() = bar()
    fun bar(): String
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int) : IFoo {
    override fun bar(): String = "OK"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L(konst x: Long) : IFoo {
    override fun bar(): String = "OK"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst x: String) : IFoo {
    override fun bar(): String = "OK"
}

fun box(): String {
    if (Z(42).foo() != "OK") throw AssertionError()
    if (L(4L).foo() != "OK") throw AssertionError()
    if (S("").foo() != "OK") throw AssertionError()

    return "OK"
}