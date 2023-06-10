// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 10 downTo 1
    for (i in ((intProgression step 2).reversed() step 3).reversed()) {
        intList += i
    }
    assertEquals(listOf(8, 5, 2), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = 10L downTo 1L
    for (i in ((longProgression step 2L).reversed() step 3L).reversed()) {
        longList += i
    }
    assertEquals(listOf(8L, 5L, 2L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = 'j' downTo 'a'
    for (i in ((charProgression step 2).reversed() step 3).reversed()) {
        charList += i
    }
    assertEquals(listOf('h', 'e', 'b'), charList)

    return "OK"
}