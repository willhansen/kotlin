// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 7 downTo 1
    for (i in intProgression step 3 step 2) {
        intList += i
    }
    assertEquals(listOf(7, 5, 3, 1), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = 7L downTo 1L
    for (i in longProgression step 3L step 2L) {
        longList += i
    }
    assertEquals(listOf(7L, 5L, 3L, 1L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = 'g' downTo 'a'
    for (i in charProgression step 3 step 2) {
        charList += i
    }
    assertEquals(listOf('g', 'e', 'c', 'a'), charList)

    return "OK"
}