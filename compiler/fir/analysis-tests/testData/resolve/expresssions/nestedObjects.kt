object A {
    object B {
        object A
    }
}

object B

konst err = B.<!UNRESOLVED_REFERENCE!>A<!>.B
konst correct = A.B.A
