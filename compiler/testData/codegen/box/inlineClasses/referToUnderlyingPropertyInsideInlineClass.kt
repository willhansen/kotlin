// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt(konst konstue: Int) {
    operator fun plus(other: UInt) = UInt(konstue + other.konstue)
    fun otherValue(other: UInt) = other.konstue
}

fun box(): String {
    konst a = UInt(10)
    konst b = UInt(20)
    if (a.otherValue(b) != 20) return "fail 1"

    if ((a + b).konstue != 30) return "fail 2"

    return "OK"
}