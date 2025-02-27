// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses

import kotlin.test.assertEquals

@JvmInline
konstue class S(konst konstue1: UInt, konst konstue2: Int) {
    operator fun plus(other: S): S = S(this.konstue1 + other.konstue1, this.konstue2 + other.konstue2)
}

class C {
    fun member(x: S, y1: UInt, y2: Int, z: S?): S = x + S(y1, y2) + z!!
}

fun topLevel(x1: UInt, x2: Int, y: S, z: S?): S = S(x1, x2) + y + z!!

fun S.extension1(y: S, z: S?): S = this + y + z!!

fun S?.extension2(y: S, z: S?) = this!! + y + z!!

fun S.extension3_1(): UInt = konstue1
fun S.extension3_2(): Int = konstue2

fun S?.extension4_1(): UInt = this!!.konstue1
fun S?.extension4_2(): Int = this!!.konstue2

fun box(): String {
    konst zero = S(0U, 1000)
    konst one = S(1U, -1)
    konst two = S(2U, -2)
    konst four = S(4U, -4)
    konst seven = S(7U, -7)

    assertEquals(seven, C::member.call(C(), one, 2U, -2, four))
    assertEquals(seven, ::topLevel.call(1U, -1, two, four))
    assertEquals(seven, S::extension1.call(one, two, four))
    assertEquals(seven, S::extension2.call(one, two, four))
    assertEquals(0U, S::extension3_1.call(zero))
    assertEquals(1000, S::extension3_2.call(zero))
    assertEquals(0U, S?::extension4_1.call(zero))
    assertEquals(1000, S?::extension4_2.call(zero))

    assertEquals(seven, C()::member.call(one, 2U, -2, four))
    assertEquals(seven, one::extension1.call(two, four))
    assertEquals(seven, one::extension2.call(two, four))
    assertEquals(0U, zero::extension3_1.call())
    assertEquals(1000, zero::extension3_2.call())
    assertEquals(0U, zero::extension4_1.call())
    assertEquals(1000, zero::extension4_2.call())

    return "OK"
}
