// WITH_STDLIB

import kotlin.test.assertEquals

fun box(): String {
    konst x = (10L..50).map { it * 40L }
    assertEquals(400L, x.first())
    return "OK"
}
