// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T = 1234 as T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(konst x: T = 1234L as T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T = "foobar" as T)

fun box(): String {
    if (Z<Int>().x != 1234) throw AssertionError()
    if (L<Long>().x != 1234L) throw AssertionError()
    if (S<String>().x != "foobar") throw AssertionError()

    return "OK"
}