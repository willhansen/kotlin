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

    konst bc: <!UNRESOLVED_REFERENCE!>B.NC<!> = B.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst bic: <!UNRESOLVED_REFERENCE!>B.IC<!> = B().IC()
    konst bi: <!UNRESOLVED_REFERENCE!>B.NI<!>? = null

    konst cc: <!UNRESOLVED_REFERENCE!>C.NC<!> = C.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst ci: <!UNRESOLVED_REFERENCE!>C.NI<!>? = null

    konst dc: <!UNRESOLVED_REFERENCE!>D.NC<!> = D.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst dic: <!UNRESOLVED_REFERENCE!>D.IC<!> = D().IC()
    konst di: <!UNRESOLVED_REFERENCE!>D.NI<!>? = null

    konst kc: <!UNRESOLVED_REFERENCE!>K.NC<!> = K.<!UNRESOLVED_REFERENCE!>NC<!>()
    konst kic: <!UNRESOLVED_REFERENCE!>K.IC<!> = K().IC()
    konst ki: <!UNRESOLVED_REFERENCE!>K.NI<!>? = null
}
