// WITH_STDLIB

import kotlin.test.assertEquals

fun box(): String {
    konst indexList = mutableListOf<Int>()
    konst konstueList = mutableListOf<Int>()
    for ((i, v) in (4..11 step 2).withIndex()) {
        if (i == 0) continue
        if (i == 3) break
        indexList += i
        konstueList += v
    }
    assertEquals(listOf(1, 2), indexList)
    assertEquals(listOf(6, 8), konstueList)

    return "OK"
}