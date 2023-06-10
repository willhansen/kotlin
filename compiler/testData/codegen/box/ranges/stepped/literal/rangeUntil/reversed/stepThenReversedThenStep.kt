// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
// DONT_TARGET_EXACT_BACKEND: JVM
// !LANGUAGE: +RangeUntilOperator
@file:OptIn(ExperimentalStdlibApi::class)
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    for (i in (1..<11 step 2).reversed() step 3) {
        intList += i
    }
    assertEquals(listOf(9, 6, 3), intList)

    konst longList = mutableListOf<Long>()
    for (i in (1L..<11L step 2L).reversed() step 3L) {
        longList += i
    }
    assertEquals(listOf(9L, 6L, 3L), longList)

    konst charList = mutableListOf<Char>()
    for (i in ('a'..<'k' step 2).reversed() step 3) {
        charList += i
    }
    assertEquals(listOf('i', 'f', 'c'), charList)

    return "OK"
}