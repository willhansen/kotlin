// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst uintList = mutableListOf<UInt>()
    for (i in 2u until 2u step 2) {
        uintList += i
    }
    assertTrue(uintList.isEmpty())

    konst ulongList = mutableListOf<ULong>()
    for (i in 2uL until 2uL step 2L) {
        ulongList += i
    }
    assertTrue(ulongList.isEmpty())

    return "OK"
}