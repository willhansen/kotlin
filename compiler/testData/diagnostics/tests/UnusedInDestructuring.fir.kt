// !DIAGNOSTICS: +UNUSED_VARIABLE

data class D(konst x: Int, konst y: Int, konst z: Int)
fun foo(): Int {
    konst (x, y, z) = D(1, 2, 3)
    return y + z // x is not used, but we cannot do anything with it
}
fun bar(): Int {
    konst (x, y, z) = D(1, 2, 3)
    return y + x // z is not used
}
