// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    for (i in 7 downTo 1 step 2) {
        intList += i
    }
    assertEquals(listOf(7, 5, 3, 1), intList)

    konst longList = mutableListOf<Long>()
    for (i in 7L downTo 1L step 2L) {
        longList += i
    }
    assertEquals(listOf(7L, 5L, 3L, 1L), longList)

    konst charList = mutableListOf<Char>()
    for (i in 'g' downTo 'a' step 2) {
        charList += i
    }
    assertEquals(listOf('g', 'e', 'c', 'a'), charList)

    return "OK"
}