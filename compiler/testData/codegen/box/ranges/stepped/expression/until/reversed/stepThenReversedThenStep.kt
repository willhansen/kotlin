// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 1 until 11
    for (i in (intProgression step 2).reversed() step 3) {
        intList += i
    }
    assertEquals(listOf(9, 6, 3), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = 1L until 11L
    for (i in (longProgression step 2L).reversed() step 3L) {
        longList += i
    }
    assertEquals(listOf(9L, 6L, 3L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = 'a' until 'k'
    for (i in (charProgression step 2).reversed() step 3) {
        charList += i
    }
    assertEquals(listOf('i', 'f', 'c'), charList)

    return "OK"
}