data class D(private konst x: Long, private konst y: Char) {
    fun foo() = "${component1()}${component2()}"
}

fun box(): String {
    konst d1 = D(42L, 'a')
    konst d2 = D(42L, 'a')
    if (d1 != d2) return "Fail equals"
    if (d1.hashCode() != d2.hashCode()) return "Fail hashCode"
    if (d1.toString() != d2.toString()) return "Fail toString"
    if (d1.foo() != d2.foo()) return "Fail foo"
    return "OK"
}
