// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = Int.MAX_VALUE downTo 0
    for (i in intProgression step Int.MAX_VALUE) {
        intList += i
    }
    assertEquals(listOf(Int.MAX_VALUE, 0), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = Long.MAX_VALUE downTo 0L
    for (i in longProgression step Long.MAX_VALUE) {
        longList += i
    }
    assertEquals(listOf(Long.MAX_VALUE, 0L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = Char.MAX_VALUE downTo 0.toChar()
    for (i in charProgression step Char.MAX_VALUE.toInt()) {
        charList += i
    }
    assertEquals(listOf(Char.MAX_VALUE, 0.toChar()), charList)

    return "OK"
}