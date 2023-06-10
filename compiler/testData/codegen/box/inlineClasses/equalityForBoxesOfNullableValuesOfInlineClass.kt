// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X(konst x: String)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Y(konst y: Number)


OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NX(konst x: String?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NY(konst y: Number?)

fun testNotNull(x: X?, y: Y?) {
    konst xs = listOf<Any?>(x)
    konst ys = listOf<Any?>(y)
    if (!xs.contains(y)) throw AssertionError()
    if (xs[0] != ys[0]) throw AssertionError()
    if (xs[0] !== ys[0]) throw AssertionError()
}

fun testNullable(x: NX?, y: NY?) {
    konst xs = listOf<Any?>(x)
    konst ys = listOf<Any?>(y)
    if (xs.contains(y)) throw AssertionError()
    if (xs[0] == ys[0]) throw AssertionError()
    if (xs[0] === ys[0]) throw AssertionError()
}

fun testNullsAsNullable(x: NX?, y: NY?) {
    konst xs = listOf<Any?>(x)
    konst ys = listOf<Any?>(y)
    if (!xs.contains(y)) throw AssertionError()
    if (xs[0] != ys[0]) throw AssertionError()
    if (xs[0] !== ys[0]) throw AssertionError()
}


fun box(): String {
    testNotNull(null, null)

    testNullable(NX(null), NY(null))
    testNullable(NX(null), null)
    testNullable(null, NY(null))

    testNullsAsNullable(null, null)

    return "OK"
}