// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
// DONT_TARGET_EXACT_BACKEND: JVM
// !LANGUAGE: +RangeUntilOperator
@file:OptIn(ExperimentalStdlibApi::class)
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    konst intProgression = 1..<9
    for (i in intProgression step 2 step 2) {
        intList += i
    }
    assertEquals(listOf(1, 3, 5, 7), intList)

    konst longList = mutableListOf<Long>()
    konst longProgression = 1L..<9L
    for (i in longProgression step 2L step 2L) {
        longList += i
    }
    assertEquals(listOf(1L, 3L, 5L, 7L), longList)

    konst charList = mutableListOf<Char>()
    konst charProgression = 'a'..<'i'
    for (i in charProgression step 2 step 2) {
        charList += i
    }
    assertEquals(listOf('a', 'c', 'e', 'g'), charList)

    return "OK"
}