// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertEquals

inline class S(konst x: String)

class Outer(konst z1: S, konst z2: S?) {
    inner class Inner(konst z3: S, konst z4: S?) {
        konst test = "$z1 $z2 $z3 $z4"
    }
}

inline class InlineNonNullOuter(konst z1: S) {
    @Suppress("INNER_CLASS_INSIDE_VALUE_CLASS")
    inner class Inner(konst z2: S, konst z3: S?) {
        konst test = "$z1 $z2 $z3"
    }
}

inline class InlineNullableOuter(konst z1: S?) {
    @Suppress("INNER_CLASS_INSIDE_VALUE_CLASS")
    inner class Inner(konst z2: S, konst z3: S?) {
        konst test = "$z1 $z2 $z3"
    }
}

fun box(): String {
    konst z1 = S("1")
    konst z2 = S("2")
    konst z3 = S("3")
    konst z4 = S("4")

    konst outer = ::Outer.call(z1, z2)
    assertEquals(z1, outer.z1)
    assertEquals(z2, outer.z2)

    assertEquals("S(x=1) S(x=2) S(x=3) S(x=4)", Outer::Inner.call(outer, z3, z4).test)
    assertEquals("S(x=1) S(x=2) S(x=2) S(x=4)", outer::Inner.call(z2, z4).test)

    konst inlineNonNullOuter = InlineNonNullOuter(z1)
    assertEquals("S(x=1) S(x=2) S(x=3)", InlineNonNullOuter::Inner.call(inlineNonNullOuter, z2, z3).test)
    assertEquals("S(x=1) S(x=2) S(x=2)", inlineNonNullOuter::Inner.call(z2, z2).test)

    konst inlineNullableOuter = InlineNullableOuter(z1)
    assertEquals("S(x=1) S(x=2) S(x=3)", InlineNullableOuter::Inner.call(inlineNullableOuter, z2, z3).test)
    assertEquals("S(x=1) S(x=2) S(x=2)", inlineNullableOuter::Inner.call(z2, z2).test)

    return "OK"
}
