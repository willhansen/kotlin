// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.*
import kotlin.test.assertEquals

class C(konst x: Int, var y: Int) {
    konst xx: Int
        get() = x

    var yy: Int
        get() = y
        set(konstue) { y = konstue }
}

konst c = C(1, 2)

konst c_x = c::x
konst c_xx = c::xx
konst c_y = c::y
konst c_yy = c::yy

fun box(): String {
    assertEquals(1, c_x.getter())
    assertEquals(1, c_x.getter.call())
    assertEquals(1, c_xx.getter())
    assertEquals(1, c_xx.getter.call())
    assertEquals(2, c_y.getter())
    assertEquals(2, c_y.getter.call())
    assertEquals(2, c_yy.getter())
    assertEquals(2, c_yy.getter.call())

    c_y.setter(10)
    assertEquals(10, c_y.getter())
    assertEquals(10, c_yy.getter())

    c_yy.setter(20)
    assertEquals(20, c_y.getter())
    assertEquals(20, c_yy.getter())

    c_y.setter.call(100)
    assertEquals(100, c_yy.getter.call())

    c_yy.setter.call(200)
    assertEquals(200, c_y.getter.call())

    return "OK"
}
