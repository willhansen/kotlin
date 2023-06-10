// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    konst uintProgression = 1u..UInt.MAX_VALUE
    for (i in uintProgression step Int.MAX_VALUE) {
        uintList += i
    }
    assertEquals(listOf(1u, 2147483648u, UInt.MAX_VALUE), uintList)

    konst ulongList = mutableListOf<ULong>()
    konst ulongProgression = 1uL..ULong.MAX_VALUE
    for (i in ulongProgression step Long.MAX_VALUE) {
        ulongList += i
    }
    assertEquals(listOf(1uL, 9223372036854775808uL, ULong.MAX_VALUE), ulongList)

    return "OK"
}