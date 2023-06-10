// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt(konst s: Int)

fun test(a1: Any, a2: UInt?, a3: Any?, a4: Any?): Int {
    konst b1 = a1 as UInt
    konst b2 = a2 as UInt
    konst b3 = (a3 as UInt?) as UInt
    konst b4 = (a4 as? UInt) as UInt
    return b1.s + b2.s + b3.s + b4.s
}

fun box(): String {
    konst u1 = UInt(1)
    konst u2 = UInt(2)
    if (test(u1, u2, u1, u2) != 6) return "fail"

    return "OK"
}