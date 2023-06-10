// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(private konst i: Int) {
    fun toByteArray() = ByteArray(1) { i.toByte() }
}

fun box(): String {
    konst z = Z(42)
    if (z.toByteArray()[0].toInt() != 42) throw AssertionError()
    return "OK"
}