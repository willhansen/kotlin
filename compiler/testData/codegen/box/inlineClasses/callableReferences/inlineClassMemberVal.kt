// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int) {
    konst xx get() = x
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L(konst x: Long) {
    konst xx get() = x
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst x: String) {
    konst xx get() = x
}

fun box(): String {
    if ((Z::xx).get(Z(42)) != 42) throw AssertionError()
    if ((L::xx).get(L(1234L)) != 1234L) throw AssertionError()
    if ((S::xx).get(S("abcdef")) != "abcdef") throw AssertionError()

    return "OK"
}