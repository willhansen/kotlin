// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

interface IBase {
    fun foo() = "BAD"
}

interface IFoo : IBase {
    override fun foo() = "OK"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T) : IFoo

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(konst x: T) : IFoo

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T) : IFoo

fun box(): String {
    if (Z(42).foo() != "OK") throw AssertionError()
    if (L(4L).foo() != "OK") throw AssertionError()
    if (S("").foo() != "OK") throw AssertionError()

    return "OK"
}