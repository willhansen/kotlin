// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// IGNORE_BACKEND: JS_IR, JS_IR_ES6
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X<T: String>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Y<T: Number>(konst y: T)


OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NX<T: String?>(konst x: T)


OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NX2<T: String>(konst x: T?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NY<T: Number?>(konst y: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NY2<T: Number>(konst y: T?)

fun testNotNull(x: X<String>?, y: Y<Number>?) {
    konst xs = listOf<Any?>(x)
    konst ys = listOf<Any?>(y)
    if (!xs.contains(y)) throw AssertionError()
    if (xs[0] != ys[0]) throw AssertionError()
    if (xs[0] !== ys[0]) throw AssertionError()
}

fun testNullable(x: NX<String?>?, y: NY<Number?>?) {
    konst xs = listOf<Any?>(x)
    konst ys = listOf<Any?>(y)
    if (xs.contains(y)) throw AssertionError()
    if (xs[0] == ys[0]) throw AssertionError()
    if (xs[0] === ys[0]) throw AssertionError()
}

fun testNullable2(x: NX2<String>?, y: NY2<Number>?) {
    konst xs = listOf<Any?>(x)
    konst ys = listOf<Any?>(y)
    if (xs.contains(y)) throw AssertionError()
    if (xs[0] == ys[0]) throw AssertionError()
    if (xs[0] === ys[0]) throw AssertionError()
}

fun testNullsAsNullable(x: NX<String?>?, y: NY<Number?>?) {
    konst xs = listOf<Any?>(x)
    konst ys = listOf<Any?>(y)
    if (!xs.contains(y)) throw AssertionError()
    if (xs[0] != ys[0]) throw AssertionError()
    if (xs[0] !== ys[0]) throw AssertionError()
}

fun testNullsAsNullable2(x: NX2<String>?, y: NY2<Number>?) {
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

    testNullable2(NX2(null), NY2(null))
    testNullable2(NX2(null), null)
    testNullable2(null, NY2(null))

    testNullsAsNullable(null, null)

    testNullsAsNullable2(null, null)

    return "OK"
}