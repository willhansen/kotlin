// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    for (i in 8u downTo 1u step 2 step 3) {
        uintList += i
    }
    assertEquals(listOf(8u, 5u, 2u), uintList)

    konst ulongList = mutableListOf<ULong>()
    for (i in 8uL downTo 1uL step 2L step 3L) {
        ulongList += i
    }
    assertEquals(listOf(8uL, 5uL, 2uL), ulongList)

    return "OK"
}