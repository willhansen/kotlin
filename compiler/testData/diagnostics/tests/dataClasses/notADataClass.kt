// FIR_IDENTICAL
class A(konst x: Int, konst y: String)

fun foo(a: A) {
    a.<!UNRESOLVED_REFERENCE!>component1<!>()
    a.<!UNRESOLVED_REFERENCE!>component2<!>()
}
