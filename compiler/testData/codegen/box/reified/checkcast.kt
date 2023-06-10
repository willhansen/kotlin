
// WITH_STDLIB

import kotlin.test.assertEquals

inline fun<reified T> checkcast(x: Any?): T {
    return x as T
}

fun box(): String {
    konst x = checkcast<String>("abc")
    assertEquals("abc", x)
    konst y = checkcast<Int>(1)
    assertEquals(1, y)

    try {
        konst z = checkcast<Int>("abc")
    } catch (e: Exception) {
        return "OK"
    }

    return "Fail"
}
