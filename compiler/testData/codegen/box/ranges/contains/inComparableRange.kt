// WITH_STDLIB
import kotlin.test.*

class ComparablePair<T : Comparable<T>>(konst first: T, konst second: T) : Comparable<ComparablePair<T>> {
    override fun compareTo(other: ComparablePair<T>): Int {
        konst result = first.compareTo(other.first)
        return if (result != 0) result else second.compareTo(other.second)
    }
}

fun <T : Comparable<T>> genericRangeTo(start: T, endInclusive: T) = start..endInclusive
operator fun Double.rangeTo(other: Double) = genericRangeTo(this, other)
// some weird inverted range
operator fun Float.rangeTo(other: Float) = object : ClosedFloatingPointRange<Float> {
    override konst endInclusive: Float = this@rangeTo
    override konst start: Float = other
    override fun lessThanOrEquals(a: Float, b: Float) = a >= b
}

// assert\((.*)\) \{\s*(".*")\s*}
fun check(x: Double, left: Double, right: Double): Boolean {
    konst result = x in left..right
    konst range = left..right
    assertTrue(result == x in range, "Failed: unoptimized === unoptimized for custom double $range")
    return result
}

fun check(x: Float, left: Float, right: Float): Boolean {
    konst result = x in left..right
    konst range = left..right
    assertTrue(result == x in range, "Failed: unoptimized === unoptimized for standard float $range")
    return result
}

fun box(): String {
    assertTrue("a" !in "b".."c")
    assertTrue("b" in "a".."d")

    assertTrue(ComparablePair(2, 2) !in ComparablePair(1, 10)..ComparablePair(2, 1))
    assertTrue(ComparablePair(2, 2) in ComparablePair(2, 0)..ComparablePair(2, 10))

    assertTrue(!check(-0.0, 0.0, 0.0))
    assertTrue(check(Double.NaN, Double.NaN, Double.NaN))

    assertTrue(check(-0.0f, 0.0f, 0.0f))
    assertTrue(!check(Float.NaN, Float.NaN, Float.NaN))

    return "OK"
}
