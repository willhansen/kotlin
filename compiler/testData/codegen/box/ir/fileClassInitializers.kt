// WITH_STDLIB
import kotlin.test.assertEquals

konst x = 1
konst y = x + 1

fun box(): String {
    assertEquals(x, 1)
    assertEquals(y, 2)

    return "OK"
}