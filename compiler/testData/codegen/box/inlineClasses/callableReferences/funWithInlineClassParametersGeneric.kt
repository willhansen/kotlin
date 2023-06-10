// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T)

fun test(aZ: Z<Int>, aL: L<Long>, aS: S<String>) = "${aZ.x} ${aL.x} ${aS.x}"

fun box(): String {
    if (::test.let { it.invoke(Z(1), L(1L), S("abc")) } != "1 1 abc") throw AssertionError()

    return "OK"
}