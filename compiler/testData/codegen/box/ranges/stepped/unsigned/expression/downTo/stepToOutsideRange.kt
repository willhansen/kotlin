// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    konst uintProgression = 7u downTo 1u
    for (i in uintProgression step 7) {
        uintList += i
    }
    assertEquals(listOf(7u), uintList)

    konst ulongList = mutableListOf<ULong>()
    konst ulongProgression = 7uL downTo 1uL
    for (i in ulongProgression step 7L) {
        ulongList += i
    }
    assertEquals(listOf(7uL), ulongList)

    return "OK"
}