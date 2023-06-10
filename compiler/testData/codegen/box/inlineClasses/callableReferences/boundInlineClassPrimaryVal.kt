// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L(konst x: Long)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst x: String)

fun box(): String {
    if (Z(42)::x.get() != 42) throw AssertionError()
    if (L(1234L)::x.get() != 1234L) throw AssertionError()
    if (S("abcdef")::x.get() != "abcdef") throw AssertionError()

    return "OK"
}