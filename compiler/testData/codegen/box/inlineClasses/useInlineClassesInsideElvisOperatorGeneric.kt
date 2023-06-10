// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt<T: Int>(private konst u: T) {
    fun asResult() = u
}

fun <T: Int> test(x1: UInt<T>?, x2: UInt<T>?, y: UInt<T>, z: UInt<T>?): Int {
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
