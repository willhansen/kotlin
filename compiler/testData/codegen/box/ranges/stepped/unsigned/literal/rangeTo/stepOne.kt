// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    for (i in 1u..4u step 1) {
        uintList += i
    }
    assertEquals(listOf(1u, 2u, 3u, 4u), uintList)

    konst ulongList = mutableListOf<ULong>()
    for (i in 1uL..4uL step 1L) {
        ulongList += i
    }
    assertEquals(listOf(1uL, 2uL, 3uL, 4uL), ulongList)

    return "OK"
}