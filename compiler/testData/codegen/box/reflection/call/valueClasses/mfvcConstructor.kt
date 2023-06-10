// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses

import kotlin.reflect.KCallable
import kotlin.test.assertEquals

@JvmInline
konstue class Z(konst x1: UInt, konst x2: Int) {
    constructor(a: UInt, b: UInt, c: Int, d: Int) : this(a + b, c + d)
}

@JvmInline
konstue class L(konst x1: ULong, konst x2: Long) {
    constructor(a: ULong, b: ULong, c: Long, d: Long) : this(a + b, c + d)
}

@JvmInline
konstue class S1(konst x1: String, konst x2: String) {
    constructor(a: String, b: String, c: String, d: String) : this(a + b, c + d)
}

@JvmInline
konstue class S2(konst x1: String?, konst x2: String?) {
    constructor(a: String?, b: String?, c: String?, d: String?) : this(a!! + b!!, c!! + d!!)
}

@JvmInline
konstue class A(konst x1: Any, konst x2: Any) {
    constructor(a: String, b: String, c: String, d: String) : this(a + b, c + d)
}

@JvmInline
konstue class Z2(konst z1: Z, konst z2: Z) {
    constructor(z1: Z, z2: Z, z3: Z, z4: Z) : this(Z(z1.x1 + z2.x1, z1.x2 + z2.x2), Z(z3.x1 + z4.x1, z3.x2 + z4.x2))
}

@JvmInline
konstue class Z3(konst z1: Z?, konst z2: Z?) {
    constructor(z1: Z?, z2: Z?, z3: Z?, z4: Z?) : this(Z(z1!!.x1 + z2!!.x1, z1!!.x2 + z2!!.x2), Z(z3!!.x1 + z4!!.x1, z3!!.x2 + z4!!.x2))
}

fun box(): String {
    konst ctorZ1_1: (UInt, Int) -> Z = ::Z
    konst ctorZ1_2: (UInt, UInt, Int, Int) -> Z = ::Z
    konst ctorL1: (ULong, Long) -> L = ::L
    konst ctorL2: (ULong, ULong, Long, Long) -> L = ::L
    konst ctorS1_1: (String, String) -> S1 = ::S1
    konst ctorS1_2: (String, String, String, String) -> S1 = ::S1
    konst ctorS2_1: (String, String) -> S2 = ::S2
    konst ctorS2_2: (String, String, String, String) -> S2 = ::S2
    konst ctorA1: (Any, Any) -> A = ::A
    konst ctorA2: (String, String, String, String) -> A = ::A
    konst ctorZ2_2: (Z, Z) -> Z2 = ::Z2
    konst ctorZ2_4: (Z, Z, Z, Z) -> Z2 = ::Z2
    konst ctorZ3_2: (Z, Z) -> Z3 = ::Z3
    konst ctorZ3_4: (Z, Z, Z, Z) -> Z3 = ::Z3

    assertEquals(Z(42U, 43), (ctorZ1_1 as KCallable<Z>).call(42U, 43))
    assertEquals(Z(123U, 224), (ctorZ1_2 as KCallable<Z>).call(100U, 23U, 200, 24))
    assertEquals(L(1UL, 2L), (ctorL1 as KCallable<L>).call(1UL, 2L))
    assertEquals(L(123UL, 224L), (ctorL2 as KCallable<L>).call(100UL, 23UL, 200L, 24L))
    assertEquals(S1("abc", "def"), (ctorS1_1 as KCallable<S1>).call("abc", "def"))
    assertEquals(S1("abc", "def"), (ctorS1_2 as KCallable<S1>).call("ab", "c", "de", "f"))
    assertEquals(S2("abc", "def"), (ctorS2_1 as KCallable<S2>).call("abc", "def"))
    assertEquals(S2("abc", "def"), (ctorS2_2 as KCallable<S2>).call("ab", "c", "de", "f"))
    assertEquals(A("abc", "def"), (ctorA1 as KCallable<A>).call("abc", "def"))
    assertEquals(A("abc", "def"), (ctorA2 as KCallable<A>).call("a", "bc", "d", "ef"))

    assertEquals(Z2(Z(42U, 43), Z(44U, 45)), (ctorZ2_2 as KCallable<Z2>).call(Z(42U, 43), Z(44U, 45)))
    assertEquals(Z3(Z(42U, 43), Z(44U, 45)), (ctorZ3_2 as KCallable<Z3>).call(Z(42U, 43), Z(44U, 45)))
    assertEquals(
        Z2(Z(142U, 243), Z(344U, 445)),
        (ctorZ2_4 as KCallable<Z2>).call(Z(42U, 43), Z(100U, 200), Z(44U, 45), Z(300U, 400))
    )
    assertEquals(
        Z3(Z(142U, 243), Z(344U, 445)),
        (ctorZ3_4 as KCallable<Z3>).call(Z(42U, 43), Z(100U, 200), Z(44U, 45), Z(300U, 400))
    )

    return "OK"
}
