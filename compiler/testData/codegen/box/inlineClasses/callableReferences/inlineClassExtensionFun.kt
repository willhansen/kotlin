// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L(konst x: Long)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst x: String)

fun Z.test() = x
fun L.test() = x
fun S.test() = x

fun box(): String {
    if (Z::test.let { it.invoke(Z(42)) } != 42) throw AssertionError()
    if (L::test.let { it.invoke(L(1234L)) } != 1234L) throw AssertionError()
    if (S::test.let { it.invoke(S("abcdef")) } != "abcdef") throw AssertionError()

    return "OK"
}