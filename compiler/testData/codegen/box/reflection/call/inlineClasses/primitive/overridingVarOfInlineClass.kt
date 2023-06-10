// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertEquals

var global = Z(0)

interface ITest {
    var nonNullTest: Z
    var nullableTest: Z?
}

inline class Z(konst x: Int) : ITest {
    override var nonNullTest: Z
        get() = Z(global.x + this.x)
        set(konstue) {
            global = Z(this.x + konstue.x)
        }

    override var nullableTest: Z?
        get() = Z(global.x + this.x)
        set(konstue) {
            global = Z(this.x + konstue!!.x)
        }
}

inline class S(konst x: String) : ITest {
    override var nonNullTest: Z
        get() = Z(global.x + x.toInt())
        set(konstue) {
            global = Z(this.x.toInt() + konstue.x)
        }

    override var nullableTest: Z?
        get() = Z(global.x + x.toInt())
        set(konstue) {
            global = Z(this.x.toInt() + konstue!!.x)
        }
}

inline class A(konst x: Any) : ITest {
    override var nonNullTest: Z
        get() = Z(global.x + this.x as Int)
        set(konstue) {
            global = Z(this.x as Int + konstue.x)
        }

    override var nullableTest: Z?
        get() = Z(global.x + this.x as Int)
        set(konstue) {
            global = Z(this.x as Int + konstue!!.x)
        }

}

fun box(): String {
    konst zZero = Z(0)
    konst zOne = Z(1)
    konst zTwo = Z(2)
    konst zThree = Z(3)
    konst zFour = Z(4)

    konst sOne = S("1")

    konst aOne = A(1)

    global = zZero
    assertEquals(zOne, Z::nonNullTest.call(zOne))
    assertEquals(zOne, zOne::nonNullTest.call())
    assertEquals(zOne, Z::nonNullTest.getter.call(zOne))
    assertEquals(zOne, zOne::nonNullTest.getter.call())
    Z::nonNullTest.setter.call(zOne, zTwo)
    assertEquals(zThree, global)
    zOne::nonNullTest.setter.call(zThree)
    assertEquals(zFour, global)

    global = zZero
    assertEquals(zOne, Z::nullableTest.call(zOne))
    assertEquals(zOne, zOne::nullableTest.call())
    assertEquals(zOne, Z::nullableTest.getter.call(zOne))
    assertEquals(zOne, zOne::nullableTest.getter.call())
    Z::nullableTest.setter.call(zOne, zTwo)
    assertEquals(zThree, global)
    zOne::nullableTest.setter.call(zThree)
    assertEquals(zFour, global)

    global = zZero
    assertEquals(zOne, S::nonNullTest.call(sOne))
    assertEquals(zOne, sOne::nonNullTest.call())
    assertEquals(zOne, S::nonNullTest.getter.call(sOne))
    assertEquals(zOne, sOne::nonNullTest.getter.call())
    S::nonNullTest.setter.call(sOne, zTwo)
    assertEquals(zThree, global)
    sOne::nonNullTest.setter.call(zThree)
    assertEquals(zFour, global)

    global = zZero
    assertEquals(zOne, S::nullableTest.call(sOne))
    assertEquals(zOne, sOne::nullableTest.call())
    assertEquals(zOne, S::nullableTest.getter.call(sOne))
    assertEquals(zOne, sOne::nullableTest.getter.call())
    S::nullableTest.setter.call(sOne, zTwo)
    assertEquals(zThree, global)
    sOne::nullableTest.setter.call(zThree)
    assertEquals(zFour, global)

    global = zZero
    assertEquals(zOne, A::nonNullTest.call(aOne))
    assertEquals(zOne, aOne::nonNullTest.call())
    assertEquals(zOne, A::nonNullTest.getter.call(aOne))
    assertEquals(zOne, aOne::nonNullTest.getter.call())
    A::nonNullTest.setter.call(aOne, zTwo)
    assertEquals(zThree, global)
    aOne::nonNullTest.setter.call(zThree)
    assertEquals(zFour, global)

    global = zZero
    assertEquals(zOne, A::nullableTest.call(aOne))
    assertEquals(zOne, aOne::nullableTest.call())
    assertEquals(zOne, A::nullableTest.getter.call(aOne))
    assertEquals(zOne, aOne::nullableTest.getter.call())
    A::nullableTest.setter.call(aOne, zTwo)
    assertEquals(zThree, global)
    aOne::nullableTest.setter.call(zThree)
    assertEquals(zFour, global)

    return "OK"
}
