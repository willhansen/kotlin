// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    for (i in 7u downTo 1u step 2) {
        uintList += i
    }
    assertEquals(listOf(7u, 5u, 3u, 1u), uintList)

    konst ulongList = mutableListOf<ULong>()
    for (i in 7uL downTo 1uL step 2L) {
        ulongList += i
    }
    assertEquals(listOf(7uL, 5uL, 3uL, 1uL), ulongList)

    return "OK"
}