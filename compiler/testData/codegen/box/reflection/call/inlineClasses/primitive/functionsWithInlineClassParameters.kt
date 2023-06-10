// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertEquals

inline class S(konst konstue: Int) {
    operator fun plus(other: S): S = S(this.konstue + other.konstue)
}

class C {
    fun member(x: S, y: Int, z: S?): S = x + S(y) + z!!
}

fun topLevel(x: Int, y: S, z: S?): S = S(x) + y + z!!

fun S.extension1(y: S, z: S?): S = this + y + z!!

fun S?.extension2(y: S, z: S?) = this!! + y + z!!

fun S.extension3(): Int = konstue

fun S?.extension4(): Int = this!!.konstue

fun box(): String {
    konst zero = S(0)
    konst one = S(1)
    konst two = S(2)
    konst four = S(4)
    konst seven = S(7)

    assertEquals(seven, C::member.call(C(), one, 2, four))
    assertEquals(seven, ::topLevel.call(1, two, four))
    assertEquals(seven, S::extension1.call(one, two, four))
    assertEquals(seven, S::extension2.call(one, two, four))
    assertEquals(0, S::extension3.call(zero))
    assertEquals(0, S?::extension4.call(zero))

    assertEquals(seven, C()::member.call(one, 2, four))
    assertEquals(seven, one::extension1.call(two, four))
    assertEquals(seven, one::extension2.call(two, four))
    assertEquals(0, zero::extension3.call())
    assertEquals(0, zero::extension4.call())

    return "OK"
}
