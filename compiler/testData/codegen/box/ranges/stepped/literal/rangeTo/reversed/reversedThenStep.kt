// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst intList = mutableListOf<Int>()
    for (i in (1..8).reversed() step 2) {
        intList += i
    }
    assertEquals(listOf(8, 6, 4, 2), intList)

    konst longList = mutableListOf<Long>()
    for (i in (1L..8L).reversed() step 2L) {
        longList += i
    }
    assertEquals(listOf(8L, 6L, 4L, 2L), longList)

    konst charList = mutableListOf<Char>()
    for (i in ('a'..'h').reversed() step 2) {
        charList += i
    }
    assertEquals(listOf('h', 'f', 'd', 'b'), charList)

    return "OK"
}