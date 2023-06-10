// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt(konst konstue: Int)

fun <T> takeVarargs(vararg e: T): T {
    return e[e.size - 1]
}

fun test(u1: UInt, u2: UInt, u3: UInt?): Int {
    konst a = takeVarargs(u1, u2)
    konst b = takeVarargs(u3) ?: UInt(-1)
    konst c = takeVarargs(u1, u3) ?: UInt(-1)

    return a.konstue + b.konstue + c.konstue
}

fun box(): String {
    konst u1 = UInt(0)
    konst u2 = UInt(1)
    konst u3 = UInt(2)
    if (test(u1, u2, u3) != 1 + 2 + 2) return "fail"

    return "OK"
}