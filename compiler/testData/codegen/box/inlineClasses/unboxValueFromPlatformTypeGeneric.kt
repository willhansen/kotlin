// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class SnekDirection<T: Int>(konst direction: T) {
    companion object {
        konst Up = SnekDirection(0)
    }
}

fun testUnbox() : SnekDirection<Int> {
    konst list = arrayListOf(SnekDirection.Up)
    return list[0]
}

fun box(): String {
    konst a = testUnbox()
    return if (a.direction == 0) "OK" else "Fail"
}