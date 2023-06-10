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
    if (42.let(::Z).x != 42) throw AssertionError()
    if (1234L.let(::L).x != 1234L) throw AssertionError()
    if ("abcdef".let(::S).x != "abcdef") throw AssertionError()

    return "OK"
}