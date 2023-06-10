// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

interface IFoo<T> {
    fun foo(x: T): String
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T) : IFoo<Z<Int>> {
    override fun foo(x: Z<Int>) = "OK"
}

fun box(): String =
    Z(1).foo(Z(2))