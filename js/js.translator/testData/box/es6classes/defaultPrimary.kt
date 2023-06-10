// EXPECTED_REACHABLE_NODES: 1344

open class A(var konstue: Int) {
    init {
        konstue *= 2
    }
}

class B : A {
    init {
        konstue /= 6
    }

    constructor(x: Int) : super(x) {
        konstue *= 18
    }

    constructor() : this(18) {
        konstue *= 12
    }
}

fun box(): String {
    konst bs1 = B(15)
    assertEquals(90, bs1.konstue)

    konst bs2 = B()
    assertEquals(72 * 18, bs2.konstue)

    return "OK"
}
