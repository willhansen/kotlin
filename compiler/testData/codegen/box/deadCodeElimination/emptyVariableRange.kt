// WITH_STDLIB

import kotlin.test.assertEquals

fun foo(): Int {
    return 1
    // konst xyz has empty live range because everything after return will be removed as dead
    konst xyz = 1
}

fun box(): String {
    assertEquals(1, foo())
    return "OK"
}
