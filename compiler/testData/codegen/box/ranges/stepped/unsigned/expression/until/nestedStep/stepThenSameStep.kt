// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    konst uintProgression = 1u until 9u
    for (i in uintProgression step 2 step 2) {
        uintList += i
    }
    assertEquals(listOf(1u, 3u, 5u, 7u), uintList)

    konst ulongList = mutableListOf<ULong>()
    konst ulongProgression = 1uL until 9uL
    for (i in ulongProgression step 2L step 2L) {
        ulongList += i
    }
    assertEquals(listOf(1uL, 3uL, 5uL, 7uL), ulongList)

    return "OK"
}