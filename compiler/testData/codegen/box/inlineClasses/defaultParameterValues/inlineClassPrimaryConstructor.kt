// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int = 1234)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L(konst x: Long = 1234L)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst x: String = "foobar")

fun box(): String {
    if (Z().x != 1234) throw AssertionError()
    if (L().x != 1234L) throw AssertionError()
    if (S().x != "foobar") throw AssertionError()

    return "OK"
}