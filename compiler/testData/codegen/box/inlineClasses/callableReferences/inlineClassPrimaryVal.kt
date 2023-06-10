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
    if ((Z::x).get(Z(42)) != 42) throw AssertionError()
    if ((L::x).get(L(1234L)) != 1234L) throw AssertionError()
    if ((S::x).get(S("abcdef")) != "abcdef") throw AssertionError()

    return "OK"
}