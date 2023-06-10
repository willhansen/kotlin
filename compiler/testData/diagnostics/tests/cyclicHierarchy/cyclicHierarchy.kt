// FIR_IDENTICAL
interface A {
    fun foo() {}
}
interface B : A, <!CYCLIC_INHERITANCE_HIERARCHY!>E<!> {}
interface C : <!CYCLIC_INHERITANCE_HIERARCHY!>B<!> {}
interface D : <!CYCLIC_INHERITANCE_HIERARCHY!>B<!> {}
interface E : <!CYCLIC_INHERITANCE_HIERARCHY!>F<!> {}
interface F : <!CYCLIC_INHERITANCE_HIERARCHY!>D<!>, <!CYCLIC_INHERITANCE_HIERARCHY!>C<!> {}
interface G : F {}
interface H : F {}

konst a : A? = null
konst b : B? = null
konst c : C? = null
konst d : D? = null
konst e : E? = null
konst f : F? = null
konst g : G? = null
konst h : H? = null

fun test() {
    a?.foo()
    b?.foo()
    c?.<!UNRESOLVED_REFERENCE!>foo<!>()
    d?.<!UNRESOLVED_REFERENCE!>foo<!>()
    e?.<!UNRESOLVED_REFERENCE!>foo<!>()
    f?.<!UNRESOLVED_REFERENCE!>foo<!>()
    g?.<!UNRESOLVED_REFERENCE!>foo<!>()
    h?.<!UNRESOLVED_REFERENCE!>foo<!>()
}