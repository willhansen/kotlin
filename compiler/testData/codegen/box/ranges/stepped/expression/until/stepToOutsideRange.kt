// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 1 until 8
    for (i in intProgression step 7) {
        intList += i
    }
    assertEquals(listOf(1), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = 1L until 8L
    for (i in longProgression step 7L) {
        longList += i
    }
    assertEquals(listOf(1L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = 'a' until 'h'
    for (i in charProgression step 7) {
        charList += i
    }
    assertEquals(listOf('a'), charList)

    return "OK"
}