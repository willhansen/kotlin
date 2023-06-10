// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt<T: Int>(konst konstue: T) {
    operator fun plus(other: UInt<T>) = UInt(konstue + other.konstue)
    fun otherValue(other: UInt<T>) = other.konstue
}

fun box(): String {
    konst a = UInt(10)
    konst b = UInt(20)
    if (a.otherValue(b) != 20) return "fail 1"

    if ((a + b).konstue != 30) return "fail 2"

    return "OK"
}