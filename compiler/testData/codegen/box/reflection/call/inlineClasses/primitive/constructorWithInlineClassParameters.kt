// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertEquals

inline class Z(konst x: Int)

class Outer(konst z1: Z, konst z2: Z?) {
    inner class Inner(konst z3: Z, konst z4: Z?) {
        konst test = "$z1 $z2 $z3 $z4"
    }
}

inline class InlineNonNullOuter(konst z1: Z) {
    @Suppress("INNER_CLASS_INSIDE_VALUE_CLASS")
    inner class Inner(konst z2: Z, konst z3: Z?) {
        konst test = "$z1 $z2 $z3"
    }
}

inline class InlineNullableOuter(konst z1: Z?) {
    @Suppress("INNER_CLASS_INSIDE_VALUE_CLASS")
    inner class Inner(konst z2: Z, konst z3: Z?) {
        konst test = "$z1 $z2 $z3"
    }
}

fun box(): String {
    konst z1 = Z(1)
    konst z2 = Z(2)
    konst z3 = Z(3)
    konst z4 = Z(4)

    konst outer = ::Outer.call(z1, z2)
    assertEquals(z1, outer.z1)
    assertEquals(z2, outer.z2)

    assertEquals("Z(x=1) Z(x=2) Z(x=3) Z(x=4)", Outer::Inner.call(outer, z3, z4).test)
    assertEquals("Z(x=1) Z(x=2) Z(x=2) Z(x=4)", outer::Inner.call(z2, z4).test)

    konst inlineNonNullOuter = InlineNonNullOuter(z1)
    assertEquals("Z(x=1) Z(x=2) Z(x=3)", InlineNonNullOuter::Inner.call(inlineNonNullOuter, z2, z3).test)
    assertEquals("Z(x=1) Z(x=2) Z(x=2)", inlineNonNullOuter::Inner.call(z2, z2).test)

    konst inlineNullableOuter = InlineNullableOuter(z1)
    assertEquals("Z(x=1) Z(x=2) Z(x=3)", InlineNullableOuter::Inner.call(inlineNullableOuter, z2, z3).test)
    assertEquals("Z(x=1) Z(x=2) Z(x=2)", inlineNullableOuter::Inner.call(z2, z2).test)

    return "OK"
}
