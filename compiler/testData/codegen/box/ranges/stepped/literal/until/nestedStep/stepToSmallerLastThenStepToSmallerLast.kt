// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    for (i in 1 until 11 step 2 step 3) {
        intList += i
    }
    assertEquals(listOf(1, 4, 7), intList)

    konst longList = mutableListOf<Long>()
    for (i in 1L until 11L step 2L step 3L) {
        longList += i
    }
    assertEquals(listOf(1L, 4L, 7L), longList)

    konst charList = mutableListOf<Char>()
    for (i in 'a' until 'k' step 2 step 3) {
        charList += i
    }
    assertEquals(listOf('a', 'd', 'g'), charList)

    return "OK"
}