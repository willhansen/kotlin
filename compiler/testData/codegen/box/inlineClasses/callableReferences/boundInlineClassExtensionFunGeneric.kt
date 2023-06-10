// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T)

fun Z<Int>.test() = x
fun L<Long>.test() = x
fun S<String>.test() = x

fun box(): String {
    if (Z(42)::test.let { it.invoke() } != 42) throw AssertionError()
    if (L(1234L)::test.let { it.invoke() } != 1234L) throw AssertionError()
    if (S("abcdef")::test.let { it.invoke() } != "abcdef") throw AssertionError()

    return "OK"
}