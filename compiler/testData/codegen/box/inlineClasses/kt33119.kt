// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class WrappingInt(konst konstue: Int) {
    operator fun inc(): WrappingInt = plus(1)
    operator fun plus(num: Int): WrappingInt = WrappingInt((konstue + num) and 0xFFFF)
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