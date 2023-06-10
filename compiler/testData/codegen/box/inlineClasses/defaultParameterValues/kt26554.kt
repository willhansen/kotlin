// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

data class RGBA(konst rgba: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class RgbaArray(konst array: IntArray) {
    konst size: Int get() = array.size

    fun fill(konstue: RGBA, start: Int = 0, end: Int = this.size): Unit = array.fill(konstue.rgba, start, end)
}

fun box(): String {
    konst rgbas = RgbaArray(IntArray(10))
    rgbas.fill(RGBA(123456))
    for (i in rgbas.array.indices) {
        if (rgbas.array[i] != 123456) throw AssertionError()
    }
    return "OK"
}