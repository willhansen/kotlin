// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    konst uintProgression = 6u downTo 1u
    for (i in uintProgression step 2 step 1) {
        uintList += i
    }
    assertEquals(listOf(6u, 5u, 4u, 3u, 2u), uintList)

    konst ulongList = mutableListOf<ULong>()
    konst ulongProgression = 6uL downTo 1uL
    for (i in ulongProgression step 2L step 1L) {
        ulongList += i
    }
    assertEquals(listOf(6uL, 5uL, 4uL, 3uL, 2uL), ulongList)

    return "OK"
}