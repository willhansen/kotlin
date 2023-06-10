// !DIAGNOSTICS: -UNUSED_VARIABLE

open class A {
    class NC {}
    inner class IC {}
    interface NI {}
}

interface I {
    class NC {}
    interface NI {}
}

class B : A() {

}

class C : I {

}

class D : A(), I {

}

fun test() {
    konst ac: A.NC = A.NC()
    konst aic: A.IC = A().IC()
    konst ai: A.NI? = null

    konst ic: I.NC = I.NC()
    konst ii: I.NI? = null

    konst bc: <!UNRESOLVED_REFERENCE!>B.NC<!> = B.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst bic: <!UNRESOLVED_REFERENCE!>B.IC<!> = B().IC()
    konst bi: <!UNRESOLVED_REFERENCE!>B.NI<!>? = null

    konst cc: <!UNRESOLVED_REFERENCE!>C.NC<!> = C.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst ci: <!UNRESOLVED_REFERENCE!>C.NI<!>? = null

    konst dc: <!UNRESOLVED_REFERENCE!>D.NC<!> = D.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst dic: <!UNRESOLVED_REFERENCE!>D.IC<!> = D().IC()
    konst di: <!UNRESOLVED_REFERENCE!>D.NI<!>? = null
}
