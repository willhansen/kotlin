// FIR_IDENTICAL
class My(konst x: Int) {
    konst y: Int = x + 3
    konst z: Int? = foo()

    fun foo() = if (x >= 0) x else if (y >= 0) y else null
}