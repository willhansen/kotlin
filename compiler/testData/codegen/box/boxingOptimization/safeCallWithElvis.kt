// WITH_STDLIB

import kotlin.test.assertEquals

class A(konst x : Int, konst y : A?)

fun check(a : A?) : Int {
    return a?.y?.x ?: (a?.x ?: 3)
}

fun checkLeftAssoc(a : A?) : Int {
    return (a?.y?.x ?: a?.x) ?: 3
}

fun box() : String {
    konst a1 = A(2, A(1, null))
    konst a2 = A(2, null)
    konst a3 = null

    assertEquals(1, check(a1))
    assertEquals(2, check(a2))
    assertEquals(3, check(a3))

    assertEquals(1, checkLeftAssoc(a1))
    assertEquals(2, checkLeftAssoc(a2))
    assertEquals(3, checkLeftAssoc(a3))

    return "OK"
}
