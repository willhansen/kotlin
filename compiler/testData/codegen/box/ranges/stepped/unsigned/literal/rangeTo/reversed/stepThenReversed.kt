// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    for (i in (1u..8u step 2).reversed()) {
        uintList += i
    }
    assertEquals(listOf(7u, 5u, 3u, 1u), uintList)

    konst ulongList = mutableListOf<ULong>()
    for (i in (1uL..8uL step 2L).reversed()) {
        ulongList += i
    }
    assertEquals(listOf(7uL, 5uL, 3uL, 1uL), ulongList)

    return "OK"
}