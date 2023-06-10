// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses

import kotlin.test.assertEquals


@JvmInline
konstue class Z(konst x1: UInt, konst x2: Int)

class Outer(konst z1: Z, konst z2: Z?) {
    inner class Inner(konst z3: Z, konst z4: Z?) {
        konst test = "$z1 $z2 $z3 $z4"
    }
}

@JvmInline
konstue class ValueNonNullOuter(konst z11: Z, konst z12: Z) {
    class Inner(konst t: ValueNonNullOuter, konst z2: Z, konst z3: Z?) {
        konst test = "${t.z11} ${t.z12} $z2 $z3"
    }
}

@JvmInline
konstue class ValueNullableOuter(konst z11: Z?, konst z12: Z?) {
    class Inner(konst t: ValueNullableOuter, konst z2: Z, konst z3: Z?) {
        konst test = "${t.z11} ${t.z12} $z2 $z3"
    }
}

fun box(): String {
    konst z1 = Z(1U, -1)
    konst z2 = Z(2U, -2)
    konst z3 = Z(3U, -3)
    konst z4 = Z(4U, -4)

    konst outer = ::Outer.call(z1, z2)
    assertEquals(z1, outer.z1)
    assertEquals(z2, outer.z2)

    assertEquals("Z(x1=1, x2=-1) Z(x1=2, x2=-2) Z(x1=3, x2=-3) Z(x1=4, x2=-4)", Outer::Inner.call(outer, z3, z4).test)
    assertEquals("Z(x1=1, x2=-1) Z(x1=2, x2=-2) Z(x1=2, x2=-2) Z(x1=4, x2=-4)", outer::Inner.call(z2, z4).test)

    konst konstueNonNullOuter = ValueNonNullOuter(z1, z4)
    assertEquals("Z(x1=1, x2=-1) Z(x1=4, x2=-4) Z(x1=2, x2=-2) Z(x1=3, x2=-3)", ValueNonNullOuter::Inner.call(konstueNonNullOuter, z2, z3).test)

    konst konstueNullableOuter = ValueNullableOuter(z1, z4)
    assertEquals("Z(x1=1, x2=-1) Z(x1=4, x2=-4) Z(x1=2, x2=-2) Z(x1=3, x2=-3)", ValueNullableOuter::Inner.call(konstueNullableOuter, z2, z3).test)

    return "OK"
}
