// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun two() = 2

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    konst uintProgression = 1u..8u
    for (i in uintProgression step two()) {
        uintList += i
    }
    assertEquals(listOf(1u, 3u, 5u, 7u), uintList)

    konst ulongList = mutableListOf<ULong>()
    konst ulongProgression = 1uL..8uL
    for (i in ulongProgression step two().toLong()) {
        ulongList += i
    }
    assertEquals(listOf(1uL, 3uL, 5uL, 7uL), ulongList)

    return "OK"
}