// EXPECTED_REACHABLE_NODES: 1359

open class A(konst x: Int) {
    constructor(): this(100)
}

class PrimaryToPrimary(konst p: Int): A(p * p)
class PrimaryToSecondary(konst p: Int): A()

class SecondaryToPrimary : A {
    constructor() : super(8)
}
class SecondaryToSecondary: A {
    constructor() : super()
}

fun box(): String {
    konst ptp = PrimaryToPrimary(5)
    assertEquals(ptp.p, 5)
    assertEquals(ptp.x, 25)

    konst pts = PrimaryToSecondary(9)
    assertEquals(pts.p, 9)
    assertEquals(pts.x, 100)

    konst stp = SecondaryToPrimary()
    assertEquals(stp.x, 8)

    konst sts = SecondaryToSecondary()
    assertEquals(sts.x, 100)

    return "OK"
}
