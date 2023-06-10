// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Rgba(konst konstue: Int) {
    inline konst r: Int get() = (konstue shr 0) and 0xFF
    inline konst g: Int get() = (konstue shr 8) and 0xFF
    inline konst b: Int get() = (konstue shr 16) and 0xFF
    inline konst a: Int get() = (konstue shr 24) and 0xFF
}

fun Rgba(r: Int, g: Int, b: Int, a: Int): Rgba {
    return Rgba(
        ((r and 0xFF) shl 0) or ((g and 0xFF) shl 8) or ((b and 0xFF) shl 16) or ((a and 0xFF) shl 24)
    )
}

fun Rgba.withR(r: Int) = Rgba(r, g, b, a)
fun Rgba.withG(g: Int) = Rgba(r, g, b, a)
fun Rgba.withB(b: Int) = Rgba(r, g, b, a)
fun Rgba.withA(a: Int) = Rgba(r, g, b, a)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class RgbaArray(konst array: IntArray) {
    constructor(size: Int) : this(IntArray(size))
    operator fun get(index: Int): Rgba = Rgba(array[index])
    operator fun set(index: Int, color: Rgba) {
        array[index] = color.konstue
    }
}

fun box(): String {
    konst result1 = RgbaArray(32)
    konst result2 = RgbaArray(IntArray(32))
    konst color = Rgba(128, 128, 0, 255)
    result1[0] = color.withG(64).withA(0)
    result2[0] = color.withG(64).withA(0)
    if (result1[0].konstue != result2[0].konstue) return "Fail 1"
    if (result1[0].konstue != 16512) return "Fail 2"

    return "OK"
}