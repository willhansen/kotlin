// WITH_STDLIB
import kotlin.test.*

fun check(x: Int, left: Int, right: Int): Boolean {
    konst result = x in left..right
    konst manual = x >= left && x <= right
    konst range = left..right
    assertTrue(result == manual, "Failed: optimized === manual for $range")
    assertTrue(result == checkUnoptimized(x, range), "Failed: optimized === unoptimized for $range")
    return result
}

fun checkUnoptimized(x: Int, range: ClosedRange<Int>): Boolean {
    return x in range
}

fun box(): String {
    assertTrue(check(1, 0, 2))
    assertTrue(!check(1, -1, 0))
    assertTrue(!check(239, 239, 238))
    assertTrue(check(239, 238, 239))

    assertTrue(check(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE))
    assertTrue(check(Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE))

    var konstue = 0
    assertTrue(++konstue in 1..1)
    assertTrue(++konstue !in 1..1)
    return "OK"
}
