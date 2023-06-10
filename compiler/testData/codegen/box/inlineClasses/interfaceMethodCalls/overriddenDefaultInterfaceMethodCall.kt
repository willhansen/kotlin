// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

interface IBase {
    fun foo() = "BAD"
}

interface IFoo : IBase {
    override fun foo() = "OK"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int) : IFoo

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L(konst x: Long) : IFoo

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst x: String) : IFoo

fun box(): String {
    if (Z(42).foo() != "OK") throw AssertionError()
    if (L(4L).foo() != "OK") throw AssertionError()
    if (S("").foo() != "OK") throw AssertionError()

    return "OK"
}