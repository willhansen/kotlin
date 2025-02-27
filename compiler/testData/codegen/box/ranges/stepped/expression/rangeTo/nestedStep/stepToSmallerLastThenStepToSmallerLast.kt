// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 1..10
    for (i in intProgression step 2 step 3) {
        intList += i
    }
    assertEquals(listOf(1, 4, 7), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = 1L..10L
    for (i in longProgression step 2L step 3L) {
        longList += i
    }
    assertEquals(listOf(1L, 4L, 7L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = 'a'..'j'
    for (i in charProgression step 2 step 3) {
        charList += i
    }
    assertEquals(listOf('a', 'd', 'g'), charList)

    return "OK"
}