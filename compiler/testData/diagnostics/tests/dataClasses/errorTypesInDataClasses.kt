// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
data class A(konst i: Int, konst j: <!UNRESOLVED_REFERENCE!>G<!>)
data class B(konst i: <!UNRESOLVED_REFERENCE!>G<!>, konst j: <!UNRESOLVED_REFERENCE!>G<!>)


fun fa(a: A) {
    konst (i, j) = a
    konst i2 = a.component1()
    konst j2 = a.component2()
}

fun fb(b: B) {
    konst (i, j) = b
    konst i2 = b.component1()
    konst j2 = b.component2()
}