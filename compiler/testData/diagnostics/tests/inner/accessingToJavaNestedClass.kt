// !DIAGNOSTICS: -UNUSED_VARIABLE

// FILE: A.java

public class A {
    static class NC {}
    class IC {}
    static interface NI {}
}

// FILE: I.java

public interface I {
    class NC {}
    interface NI {}
}

// FILE: B.java

public class B extends A {

}

// FILE: C.java

public class C implements I {

}

// FILE: D.java

public class D extends A implements I {

}

// FILE: K.kt

class K : D()

// FILE: test.kt

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

    konst kc: K.<!UNRESOLVED_REFERENCE!>NC<!> = K.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst kic: K.<!UNRESOLVED_REFERENCE!>IC<!> = K().IC()
    konst ki: K.<!UNRESOLVED_REFERENCE!>NI<!>? = null
}
