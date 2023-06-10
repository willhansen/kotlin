// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 1 until 8
    for (i in intProgression step 3 step 2) {
        intList += i
    }
    assertEquals(listOf(1, 3, 5, 7), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = 1L until 8L
    for (i in longProgression step 3L step 2L) {
        longList += i
    }
    assertEquals(listOf(1L, 3L, 5L, 7L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = 'a' until 'h'
    for (i in charProgression step 3 step 2) {
        charList += i
    }
    assertEquals(listOf('a', 'c', 'e', 'g'), charList)

    return "OK"
}