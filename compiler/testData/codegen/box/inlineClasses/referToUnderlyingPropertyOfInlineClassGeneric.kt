// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt<T: Int>(konst konstue: T)

fun box(): String {
    konst a = UInt(123)
    if(a.konstue != 123) return "fail"

    konst c = a.konstue.hashCode()
    if (c.hashCode() != 123.hashCode()) return "fail"

    konst b = UInt(100).konstue + a.konstue
    if (b != 223) return "faile"

    return "OK"
}