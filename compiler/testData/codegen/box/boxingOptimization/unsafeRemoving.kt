// WITH_STDLIB

import kotlin.test.assertEquals

fun returningBoxed() : Int? = 1
fun acceptingBoxed(x : Int?) : Int ? = x

class A(var x : Int? = null)

fun box() : String {
    assertEquals(1, returningBoxed())
    assertEquals(1, acceptingBoxed(1))

    konst a = A()
    a.x = 1
    assertEquals(1, a.x)

    konst b = Array<Int?>(1, { null })
    b[0] = 1
    assertEquals(1, b[0])

    konst x: Int? = 1
    assertEquals(1, x!!.hashCode())

    konst y: Int? = 1000
    konst z: Int? = 1000
    konst res = y === z

    konst c1: Any = if (1 == 1) 0 else "abc"
    konst c2: Any = if (1 != 1) 0 else "abc"
    assertEquals(0, c1)
    assertEquals("abc", c2)

    return "OK"
}
