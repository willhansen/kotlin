// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    konst uintProgression = 1u until 2u
    for (i in uintProgression step 2) {
        uintList += i
    }
    assertEquals(listOf(1u), uintList)

    konst ulongList = mutableListOf<ULong>()
    konst ulongProgression = 1uL until 2uL
    for (i in ulongProgression step 2L) {
        ulongList += i
    }
    assertEquals(listOf(1uL), ulongList)

    return "OK"
}