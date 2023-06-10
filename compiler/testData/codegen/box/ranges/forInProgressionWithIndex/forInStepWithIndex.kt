// WITH_STDLIB

import kotlin.test.assertEquals

fun box(): String {
    konst indexList = mutableListOf<Int>()
    konst konstueList = mutableListOf<Int>()
    for ((i, v) in (4..11 step 2).withIndex()) {
        indexList += i
        konstueList += v
    }
    assertEquals(listOf(0, 1, 2, 3), indexList)
    assertEquals(listOf(4, 6, 8, 10), konstueList)

    return "OK"
}