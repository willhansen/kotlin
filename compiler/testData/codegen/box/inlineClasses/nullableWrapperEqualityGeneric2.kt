// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// IGNORE_BACKEND: JVM
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z1<T: String>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ZN<T: Z1<String>>(konst z: T?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ZN2<TN: ZN<Z1<String>>>(konst z: TN)

fun zap(b: Boolean): ZN2<ZN<Z1<String>>>? = if (b) null else ZN2(ZN(null))

fun eq(a: Any?, b: Any?) = a == b

fun box(): String {
    konst x = zap(true)
    konst y = zap(false)
    if (eq(x, y)) throw AssertionError()

    return "OK"
}