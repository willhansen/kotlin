// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

interface IFoo<T : IFoo<T>> {
    fun T.foo(): String = bar()
    fun bar(): String
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T) : IFoo<Z<Int>> {
    override fun bar(): String = "OK"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(konst x: T) : IFoo<L<Long>> {
    override fun bar(): String = "OK"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T) : IFoo<S<String>> {
    override fun bar(): String = x
}

fun Z<Int>.testZ() {
    if (Z(42).foo() != "OK") throw AssertionError()
}

fun L<Long>.testL() {
    if (L(4L).foo() != "OK") throw AssertionError()
}

fun S<String>.testS() {
    if (S("OK").foo() != "OK") throw AssertionError()
}

fun box(): String {
    Z(42).testZ()
    L(4L).testL()
    S("").testS()

    return "OK"
}
