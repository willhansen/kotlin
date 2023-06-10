// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
interface T<E> {
    fun f() : E = null!!
}
open class A<X>() {
    inner class B() : T<X> {}
}

fun test() {
    konst a = A<Int>()
    konst b : A<Int>.B = a.B()
}