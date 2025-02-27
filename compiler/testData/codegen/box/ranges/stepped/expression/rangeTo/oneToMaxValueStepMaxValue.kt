// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 1..Int.MAX_VALUE
    for (i in intProgression step Int.MAX_VALUE) {
        intList += i
    }
    assertEquals(listOf(1), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = 1L..Long.MAX_VALUE
    for (i in longProgression step Long.MAX_VALUE) {
        longList += i
    }
    assertEquals(listOf(1L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = 1.toChar()..Char.MAX_VALUE
    for (i in charProgression step Char.MAX_VALUE.toInt()) {
        charList += i
    }
    assertEquals(listOf(1.toChar()), charList)

    return "OK"
}