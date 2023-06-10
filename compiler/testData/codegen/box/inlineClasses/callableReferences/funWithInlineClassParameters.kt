// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L(konst x: Long)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst x: String)

fun test(aZ: Z, aL: L, aS: S) = "${aZ.x} ${aL.x} ${aS.x}"

fun box(): String {
    if (::test.let { it.invoke(Z(1), L(1L), S("abc")) } != "1 1 abc") throw AssertionError()

    return "OK"
}