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

    konst bc: B.<!UNRESOLVED_REFERENCE!>NC<!> = B.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst bic: B.<!UNRESOLVED_REFERENCE!>IC<!> = B().IC()
    konst bi: B.<!UNRESOLVED_REFERENCE!>NI<!>? = null

    konst cc: C.<!UNRESOLVED_REFERENCE!>NC<!> = C.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst ci: C.<!UNRESOLVED_REFERENCE!>NI<!>? = null

    konst dc: D.<!UNRESOLVED_REFERENCE!>NC<!> = D.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst dic: D.<!UNRESOLVED_REFERENCE!>IC<!> = D().IC()
    konst di: D.<!UNRESOLVED_REFERENCE!>NI<!>? = null
}
