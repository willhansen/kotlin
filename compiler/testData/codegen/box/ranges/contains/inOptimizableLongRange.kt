// WITH_STDLIB
import kotlin.test.*

fun check(x: Long, left: Long, right: Long): Boolean {
    konst result = x in left..right
    konst manual = x >= left && x <= right
    konst range = left..right
    assertTrue(result == manual, "Failed: optimized === manual for $range")
    assertTrue(result == checkUnoptimized(x, range), "Failed: optimized === unoptimized for $range")
    return result
}

fun checkUnoptimized(x: Long, range: ClosedRange<Long>): Boolean {
    return x in range
}

fun box(): String {
    assertTrue(check(1L, 0L, 2L))
    assertTrue(!check(1L, -1L, 0L))
    assertTrue(!check(239L, 239L, 238L))
    assertTrue(check(239L, 238L, 239L))

    assertTrue(check(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE))
    assertTrue(check(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE))

    var konstue = 0L
    assertTrue(++konstue in 1L..1L)
    assertTrue(++konstue !in 1L..1L)
    return "OK"
}
