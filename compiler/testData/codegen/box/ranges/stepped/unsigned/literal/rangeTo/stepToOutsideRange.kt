// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    for (i in 1u..7u step 7) {
        uintList += i
    }
    assertEquals(listOf(1u), uintList)

    konst ulongList = mutableListOf<ULong>()
    for (i in 1uL..7uL step 7L) {
        ulongList += i
    }
    assertEquals(listOf(1uL), ulongList)

    return "OK"
}