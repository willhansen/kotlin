// FIR_IDENTICAL
// FILE: A.java
public interface A {
    public class A_S { // static

    }
}

// FILE: B.java
public class B {
    public static class B_S {

    }
    public class B_ {

    }
}

// FILE: C.java
public class C extends B implements A {

}

// FILE: 1.kt

class X: A {
    konst a_s: <!UNRESOLVED_REFERENCE!>A_S<!> = null!!

    init {
        <!UNRESOLVED_REFERENCE!>A_S<!>()
        A.A_S()
        X.<!UNRESOLVED_REFERENCE!>A_S<!>()
    }

    object xD {
        konst a_: <!UNRESOLVED_REFERENCE!>A_S<!> = null!!

        init {
            <!UNRESOLVED_REFERENCE!>A_S<!>()
        }
    }
}

class Y: B() {
    konst b_: B_ = null!!
    konst b_s: B_S = null!!

    init {
        B_()
        B.<!RESOLUTION_TO_CLASSIFIER!>B_<!>()
        Y.<!UNRESOLVED_REFERENCE!>B_<!>()

        B_S()
        B.B_S()
        Y.<!UNRESOLVED_REFERENCE!>B_S<!>()
    }

    object X {
        konst b_: B_ = null!!
        konst b_s: B_S = null!!

        init {
            <!RESOLUTION_TO_CLASSIFIER!>B_<!>()
            B_S()
        }
    }
}

class Z: C() {
    konst a_s: <!UNRESOLVED_REFERENCE!>A_S<!> = null!!
    konst b_: B_ = null!!
    konst b_s: B_S = null!!

    init {
        <!UNRESOLVED_REFERENCE!>A_S<!>()
        B_()
        B_S()
    }

    object X {
        konst a_s: <!UNRESOLVED_REFERENCE!>A_S<!> = null!!
        konst b_: B_ = null!!
        konst b_s: B_S = null!!

        init {
            <!UNRESOLVED_REFERENCE!>A_S<!>()
            <!RESOLUTION_TO_CLASSIFIER!>B_<!>()
            B_S()
        }
    }
}
