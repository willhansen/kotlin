// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt(private konst u: Int) {
    fun asResult() = u
}

fun test(x1: UInt?, x2: UInt?, y: UInt, z: UInt?): Int {
    konst a = x1 ?: y
    konst b = x1 ?: z!!
    konst c = x1 ?: x2 ?: y
    return a.asResult() + b.asResult() + c.asResult()
}

fun box(): String {
    konst u1 = UInt(10)
    konst u2 = UInt(20)
    konst r = test(null, null, u1, u2)
    return if (r != 40) "fail: $r" else "OK"
}
