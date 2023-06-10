// EXPECTED_REACHABLE_NODES: 1340

open class A(konst x: Int)

class B(konst p: Int, konst q: Int): A(p + q)

fun box(): String {
    konst b = B(2, 3)
    assertEquals(b.p, 2)
    assertEquals(b.q, 3)
    assertEquals(b.x, 5)

    return "OK"
}
