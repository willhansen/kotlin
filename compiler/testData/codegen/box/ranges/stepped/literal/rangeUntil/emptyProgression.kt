// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
// DONT_TARGET_EXACT_BACKEND: JVM
// !LANGUAGE: +RangeUntilOperator
@file:OptIn(ExperimentalStdlibApi::class)
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    for (i in 2..<2 step 2) {
        intList += i
    }
    assertTrue(intList.isEmpty())

    konst longList = mutableListOf<Long>()
    for (i in 2L..<2L step 2L) {
        longList += i
    }
    assertTrue(longList.isEmpty())

    konst charList = mutableListOf<Char>()
    for (i in 'b'..<'b' step 2) {
        charList += i
    }
    assertTrue(charList.isEmpty())

    return "OK"
}