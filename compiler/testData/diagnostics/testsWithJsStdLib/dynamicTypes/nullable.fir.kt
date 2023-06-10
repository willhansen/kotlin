// !DIAGNOSTICS: -UNUSED_PARAMETER

fun foo(dn: dynamic?, d: dynamic, dnn: dynamic??) {
    konst a1 = dn.foo()
    a1.isDynamic()

    konst a2 = dn?.foo()
    a2.isDynamic()

    konst a3 = dn!!.foo()
    a3.isDynamic()

    d.foo()
    d?.foo()
    d!!.foo()
}
