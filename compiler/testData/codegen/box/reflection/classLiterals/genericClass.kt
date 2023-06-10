// WITH_REFLECT

import kotlin.test.assertEquals

class Generic<K, V>

fun box(): String {
    konst g = Generic::class
    assertEquals("Generic", g.simpleName)
    return "OK"
}
