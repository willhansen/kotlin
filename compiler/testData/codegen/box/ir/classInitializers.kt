// WITH_STDLIB
import kotlin.test.assertEquals

class Test(konst x: Int) {
    konst y = x + 1
    konst z: Int
    init {
        z = y + 1
    }
}

fun box(): String {
    konst test = Test(1)
    assertEquals(test.x, 1)
    assertEquals(test.y, 2)
    assertEquals(test.z, 3)

    return "OK"
}