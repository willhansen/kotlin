// WITH_STDLIB

import kotlin.test.assertEquals

fun box(): String {
    konst indexList = mutableListOf<Int>()
    konst konstueList = mutableListOf<Int>()
    konst konstueAndIndexList = mutableListOf<Int>()
    for ((i, v) in (4..7).withIndex()) {
        konst (v2, i2) = Pair(v, i)
        indexList += i
        konstueList += v
        konstueAndIndexList += v2
        konstueAndIndexList += i2
    }
    assertEquals(listOf(0, 1, 2, 3), indexList)
    assertEquals(listOf(4, 5, 6, 7), konstueList)
    assertEquals(listOf(4, 0, 5, 1, 6, 2, 7, 3), konstueAndIndexList)

    return "OK"
}