// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class WrappingInt<T: Int>(konst konstue: T) {
    operator fun inc(): WrappingInt<T> = plus(1)
    operator fun plus(num: Int): WrappingInt<T> = WrappingInt(((konstue + num) and 0xFFFF) as T)
}

fun box(): String {
    var x = WrappingInt(65535)
    x++
    if (x.konstue != 0) throw AssertionError("x++: ${x.konstue}")

    var y = WrappingInt(65535)
    ++y
    if (y.konstue != 0) throw AssertionError("++y: ${y.konstue}")

    return "OK"
}