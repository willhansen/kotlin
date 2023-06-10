// FIR_IDENTICAL
// FILE: A.java
public interface A {
    public class A_ {}
}

// FILE: 1.kt
interface B {
    class B_
}

class X: A {
    konst a: <!UNRESOLVED_REFERENCE!>A_<!> = <!UNRESOLVED_REFERENCE!>A_<!>()
    konst b: A.A_ = A.A_()

    companion object {
        konst a: <!UNRESOLVED_REFERENCE!>A_<!> = <!UNRESOLVED_REFERENCE!>A_<!>()
    }
}

class Y: B {
    konst a: <!UNRESOLVED_REFERENCE!>B_<!> = <!UNRESOLVED_REFERENCE!>B_<!>()
    konst b: B.B_ = B.B_()

    companion object {
        konst b: <!UNRESOLVED_REFERENCE!>B_<!> = <!UNRESOLVED_REFERENCE!>B_<!>()
    }
}
