// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    konst uintProgression = 10u downTo 1u
    for (i in uintProgression step 3 step 2) {
        uintList += i
    }
    assertEquals(listOf(10u, 8u, 6u, 4u, 2u), uintList)

    konst ulongList = mutableListOf<ULong>()
    konst ulongProgression = 10uL downTo 1uL
    for (i in ulongProgression step 3L step 2L) {
        ulongList += i
    }
    assertEquals(listOf(10uL, 8uL, 6uL, 4uL, 2uL), ulongList)

    return "OK"
}