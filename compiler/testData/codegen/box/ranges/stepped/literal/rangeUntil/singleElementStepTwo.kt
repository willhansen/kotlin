// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
// DONT_TARGET_EXACT_BACKEND: JVM
// !LANGUAGE: +RangeUntilOperator
@file:OptIn(ExperimentalStdlibApi::class)
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    for (i in 1..<2 step 2) {
        intList += i
    }
    assertEquals(listOf(1), intList)

    konst longList = mutableListOf<Long>()
    for (i in 1L..<2L step 2L) {
        longList += i
    }
    assertEquals(listOf(1L), longList)

    konst charList = mutableListOf<Char>()
    for (i in 'a'..<'b' step 2) {
        charList += i
    }
    assertEquals(listOf('a'), charList)

    return "OK"
}