// FIR_IDENTICAL
fun test(d: dynamic) {
    konst v1 = d?.foo()
    v1.isDynamic() // to check that anything is resolvable

    konst v2 = d!!.foo(1)
    v2.isDynamic() // to check that anything is resolvable
}