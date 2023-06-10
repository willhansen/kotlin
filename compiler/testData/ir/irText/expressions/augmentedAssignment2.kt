class A

operator fun A.plusAssign(s: String) {}
operator fun A.minusAssign(s: String) {}
operator fun A.timesAssign(s: String) {}
operator fun A.divAssign(s: String) {}
operator fun A.remAssign(s: String) {}

konst p = A()

fun testVariable() {
    konst a = A()
    a += "+="
    a -= "-="
    a *= "*="
    a /= "/="
    a %= "*="
}

fun testProperty() {
    p += "+="
    p -= "-="
    p *= "*="
    p /= "/="
    p %= "%="
}
