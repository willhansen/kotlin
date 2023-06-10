// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 4 downTo 1
    for (i in intProgression step 1 step 1) {
        intList += i
    }
    assertEquals(listOf(4, 3, 2, 1), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = 4L downTo 1L
    for (i in longProgression step 1L step 1L) {
        longList += i
    }
    assertEquals(listOf(4L, 3L, 2L, 1L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = 'd' downTo 'a'
    for (i in charProgression step 1 step 1) {
        charList += i
    }
    assertEquals(listOf('d', 'c', 'b', 'a'), charList)

    return "OK"
}