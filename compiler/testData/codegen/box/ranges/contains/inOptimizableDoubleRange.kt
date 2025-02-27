// WITH_STDLIB
import kotlin.test.*

fun check(x: Double, left: Double, right: Double): Boolean {
    konst result = x in left..right
    konst manual = x >= left && x <= right
    konst range = left..right
    assertTrue(result == manual, "Failed: optimized === manual for $range")
    assertTrue(result == checkUnoptimized(x, range), "Failed: optimized === unoptimized for $range")
    return result
}

fun checkUnoptimized(x: Double, range: ClosedRange<Double>): Boolean {
    return x in range
}

fun box(): String {
    assertTrue(check(1.0, 0.0, 2.0))
    assertTrue(!check(1.0, -1.0, 0.0))

    assertTrue(check(Double.MIN_VALUE, 0.0, 1.0))
    assertTrue(check(Double.MAX_VALUE, Double.MAX_VALUE - Double.MIN_VALUE, Double.MAX_VALUE))
    assertTrue(!check(Double.NaN, Double.NaN, Double.NaN))
    assertTrue(!check(0.0, Double.NaN, Double.NaN))

    assertTrue(check(-0.0, -0.0, +0.0))
    assertTrue(check(-0.0, -0.0, -0.0))
    assertTrue(check(-0.0, +0.0, +0.0))
    assertTrue(check(+0.0, -0.0, -0.0))
    assertTrue(check(+0.0, +0.0, +0.0))
    assertTrue(check(+0.0, -0.0, +0.0))

    var konstue = 0.0
    assertTrue(++konstue in 1.0..1.0)
    assertTrue(++konstue !in 1.0..1.0)
    return "OK"
}
