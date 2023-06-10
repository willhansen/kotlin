// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z1(konst x: String)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ZN(konst z: Z1?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ZN2(konst z: ZN)

fun zap(b: Boolean): ZN2? = if (b) null else ZN2(ZN(null))

fun eq(a: Any?, b: Any?) = a == b

fun box(): String {
    konst x = zap(true)
    konst y = zap(false)
    if (eq(x, y)) throw AssertionError()

    return "OK"
}