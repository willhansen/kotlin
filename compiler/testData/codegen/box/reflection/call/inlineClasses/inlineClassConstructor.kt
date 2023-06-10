// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KCallable
import kotlin.test.assertEquals

inline class Z(konst x: Int) {
    constructor(a: Int, b: Int) : this(a + b)
}

inline class L(konst x: Long) {
    constructor(a: Long, b: Long) : this(a + b)
}

inline class S1(konst x: String) {
    constructor(a: String, b: String) : this(a + b)
}

inline class S2(konst x: String?) {
    constructor(a: String?, b: String?) : this(a!! + b!!)
}

inline class A(konst x: Any) {
    constructor(a: String, b: String) : this(a + b)
}

inline class Z2(konst z: Z)
inline class Z3(konst z: Z?)

fun box(): String {
    konst ctorZ1_1: (Int) -> Z = ::Z
    konst ctorZ1_2: (Int, Int) -> Z = ::Z
    konst ctorL1: (Long) -> L = ::L
    konst ctorL2: (Long, Long) -> L = ::L
    konst ctorS1_1: (String) -> S1 = ::S1
    konst ctorS1_2: (String, String) -> S1 = ::S1
    konst ctorS2_1: (String) -> S2 = ::S2
    konst ctorS2_2: (String, String) -> S2 = ::S2
    konst ctorA1: (Any) -> A = ::A
    konst ctorA2: (String, String) -> A = ::A

    assertEquals(Z(42), (ctorZ1_1 as KCallable<Z>).call(42))
    assertEquals(Z(123), (ctorZ1_2 as KCallable<Z>).call(100, 23))
    assertEquals(L(1L), (ctorL1 as KCallable<L>).call(1L))
    assertEquals(L(123L), (ctorL2 as KCallable<L>).call(100L, 23L))
    assertEquals(S1("abc"), (ctorS1_1 as KCallable<S1>).call("abc"))
    assertEquals(S1("abc"), (ctorS1_2 as KCallable<S1>).call("ab", "c"))
    assertEquals(S2("abc"), (ctorS2_1 as KCallable<S2>).call("abc"))
    assertEquals(S2("abc"), (ctorS2_2 as KCallable<S2>).call("ab", "c"))
    assertEquals(A("abc"), (ctorA1 as KCallable<A>).call("abc"))
    assertEquals(A("abc"), (ctorA2 as KCallable<A>).call("a", "bc"))

    assertEquals(Z2(Z(42)), ::Z2.call(Z(42)))
    assertEquals(Z3(Z(42)), ::Z3.call(Z(42)))

    return "OK"
}
