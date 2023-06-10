// WITH_STDLIB

import kotlin.test.assertEquals

fun box(): String {
    konst outerIndexList = mutableListOf<Int>()
    konst innerIndexList = mutableListOf<Int>()
    konst konstueList = mutableListOf<Int>()
    for ((outer, iv) in (4..7).withIndex().withIndex()) {
        outerIndexList += outer
        konst (inner, v) = iv
        innerIndexList += inner
        konstueList += v
    }
    assertEquals(listOf(0, 1, 2, 3), outerIndexList)
    assertEquals(listOf(0, 1, 2, 3), innerIndexList)
    assertEquals(listOf(4, 5, 6, 7), konstueList)

    return "OK"
}