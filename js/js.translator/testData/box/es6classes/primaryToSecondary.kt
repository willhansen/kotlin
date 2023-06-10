// EXPECTED_REACHABLE_NODES: 1341

open class A(konst x: Int, konst y: Int) {
    constructor(x: Int) : this(x, x)
}

class B(x: Int) : A(x)

fun box(): String {
    konst b = B(45)

    assertEquals(b.x, 45)
    assertEquals(b.y, 45)

    return "OK"
}
