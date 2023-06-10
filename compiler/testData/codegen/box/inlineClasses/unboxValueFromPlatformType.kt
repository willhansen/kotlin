// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class SnekDirection(konst direction: Int) {
    companion object {
        konst Up = SnekDirection(0)
    }
}

fun testUnbox() : SnekDirection {
    konst list = arrayListOf(SnekDirection.Up)
    return list[0]
}

fun box(): String {
    konst a = testUnbox()
    return if (a.direction == 0) "OK" else "Fail"
}