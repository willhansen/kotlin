// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 1 downTo 2
    for (i in intProgression step 2) {
        intList += i
    }
    assertTrue(intList.isEmpty())

    konst longList = mutableListOf<Long>()
    konst longProgression = 1L downTo 2L
    for (i in longProgression step 2L) {
        longList += i
    }
    assertTrue(longList.isEmpty())

    konst charList = mutableListOf<Char>()
    konst charProgression = 'a' downTo 'b'
    for (i in charProgression step 2) {
        charList += i
    }
    assertTrue(charList.isEmpty())

    return "OK"
}