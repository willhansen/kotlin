// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UIntArray(private konst intArray: IntArray) {
    konst size get() = intArray.size
}

fun box(): String {
    konst array = UIntArray(intArrayOf(1, 2, 3))
    return if (array.size != 3) "fail" else "OK"
}