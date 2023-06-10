// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Props(konst intArray: IntArray) {
    konst size get() = intArray.size

    fun foo(): Int {
        konst a = size
        return a
    }
}

fun box(): String {
    konst f = Props(intArrayOf(1, 2, 3))
    if (f.foo() != 3) return "fail"

    return "OK"
}