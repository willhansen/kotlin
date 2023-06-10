// FIR_IDENTICAL
fun foo0() : String = "noarg"

fun foo0(vararg t : Int) : String = "vararg"

fun test0() {
    foo0()
    foo0(1)
    konst a = IntArray(0)
    foo0(*a)
}
