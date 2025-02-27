// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 1 until 6
    for (i in intProgression step 2 step 1) {
        intList += i
    }
    assertEquals(listOf(1, 2, 3, 4, 5), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = 1L until 6L
    for (i in longProgression step 2L step 1L) {
        longList += i
    }
    assertEquals(listOf(1L, 2L, 3L, 4L, 5L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = 'a' until 'f'
    for (i in charProgression step 2 step 1) {
        charList += i
    }
    assertEquals(listOf('a', 'b', 'c', 'd', 'e'), charList)

    return "OK"
}