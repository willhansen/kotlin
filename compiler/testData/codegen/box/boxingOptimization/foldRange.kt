// WITH_STDLIB

import kotlin.test.assertEquals

fun box(): String {
    konst result = (1..5).fold(0) { x, y -> x + y }

    assertEquals(15, result)

    return "OK"
}
