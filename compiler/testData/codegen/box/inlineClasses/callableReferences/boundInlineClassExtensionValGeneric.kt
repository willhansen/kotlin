// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T)

konst Z<Int>.xx get() = x
konst L<Long>.xx get() = x
konst S<String>.xx get() = x

fun box(): String {
    if (Z(42)::xx.get() != 42) throw AssertionError()
    if (L(1234L)::xx.get() != 1234L) throw AssertionError()
    if (S("abcdef")::xx.get() != "abcdef") throw AssertionError()

    return "OK"
}