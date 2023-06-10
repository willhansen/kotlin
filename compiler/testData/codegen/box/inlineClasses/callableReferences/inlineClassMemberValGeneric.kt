// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T) {
    konst xx get() = x
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(konst x: T) {
    konst xx get() = x
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T) {
    konst xx get() = x
}

fun box(): String {
    if ((Z<Int>::xx).get(Z(42)) != 42) throw AssertionError()
    if ((L<Long>::xx).get(L(1234L)) != 1234L) throw AssertionError()
    if ((S<String>::xx).get(S("abcdef")) != "abcdef") throw AssertionError()

    return "OK"
}