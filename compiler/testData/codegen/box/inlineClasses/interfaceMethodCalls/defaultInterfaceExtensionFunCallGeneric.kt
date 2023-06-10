// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

interface IFoo {
    fun Long.foo() = bar()
    fun bar(): String
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T) : IFoo {
    override fun bar(): String = "OK"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(konst x: T) : IFoo {
    override fun bar(): String = "OK"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T) : IFoo {
    override fun bar(): String = "OK"
}

fun Z<Int>.testZ() {
    if (1L.foo() != "OK") throw AssertionError()
}

fun L<Long>.testL() {
    if (1L.foo() != "OK") throw AssertionError()
}

fun S<String>.testS() {
    if (1L.foo() != "OK") throw AssertionError()
}

fun box(): String {
    Z(42).testZ()
    L(4L).testL()
    S("").testS()

    return "OK"
}