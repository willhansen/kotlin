// FIR_IDENTICAL
fun test(d: dynamic) {
    konst v1 = d.foo()
    v1.isDynamic() // to check that anything is resolvable

    konst v2 = d.foo(1)
    v2.isDynamic() // to check that anything is resolvable

    konst v3 = d.foo(1, "")
    v3.isDynamic() // to check that anything is resolvable

    konst v4 = d.foo<String>()
    v4.isDynamic() // to check that anything is resolvable

    konst v5 = d.foo
    v5.isDynamic() // to check that anything is resolvable

    d.foo = 1
}