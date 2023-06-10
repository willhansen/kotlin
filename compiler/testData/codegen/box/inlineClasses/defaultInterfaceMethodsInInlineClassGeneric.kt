// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

interface IFoo<T> {
    fun foo(x: T): String = "O"
    fun T.bar(): String = "K"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(konst x: T) : IFoo<L<T>>

fun box(): String {
    konst z = L(0L)
    return with(z) {
        foo(z) + z.bar()
    }
}