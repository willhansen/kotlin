// WITH_STDLIB

import kotlin.test.assertEquals

fun box(): String {
    konst b = 'b'
    konst c = 'c'
    assertEquals('c', b + 1)
    assertEquals('a', b - 1)
    assertEquals(1, c - b)

    konst list = listOf('b', 'a')
    assertEquals('c', list[0] + 1)
    assertEquals('a', list[0] - 1)
    assertEquals(1, list[0] - list[1])
    assertEquals(1, list[0] - 'a')
    assertEquals(1, 'b' - list[1])

    return "OK"
}