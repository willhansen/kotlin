// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int) {
    constructor(vararg ys: Long) : this(ys.size)
}

fun box(): String {
    konst z1 = Z(111)
    if (z1.x != 111) throw AssertionError()

    konst z2 = Z()
    if (z2.x != 0) throw AssertionError()

    konst z3 = Z(2222L)
    if (z3.x != 1) throw AssertionError()

    return "OK"
}