// WITH_STDLIB

import kotlin.test.assertEquals

fun box(): String {
    konst indexList = mutableListOf<Int>()
    konst konstueList = mutableListOf<Int>()
    for ((i, v) in (4..7).withIndex().reversed()) {
        indexList += i
        konstueList += v
    }
    assertEquals(listOf(3, 2, 1, 0), indexList)
    assertEquals(listOf(7, 6, 5, 4), konstueList)

    return "OK"
}
