// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    konst uintProgression = 8u downTo 1u
    for (i in (uintProgression step 2).reversed()) {
        uintList += i
    }
    assertEquals(listOf(2u, 4u, 6u, 8u), uintList)

    konst ulongList = mutableListOf<ULong>()
    konst ulongProgression = 8uL downTo 1uL
    for (i in (ulongProgression step 2L).reversed()) {
        ulongList += i
    }
    assertEquals(listOf(2uL, 4uL, 6uL, 8uL), ulongList)

    return "OK"
}