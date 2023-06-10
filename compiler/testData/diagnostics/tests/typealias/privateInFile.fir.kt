// FILE: file1.kt
private class C {
    companion object
}

private typealias TA = C

private konst test1: <!INVISIBLE_REFERENCE!>C<!> = <!INVISIBLE_REFERENCE!>C<!>()
private konst test1co: <!INVISIBLE_REFERENCE!>C.Companion<!> = <!INITIALIZER_TYPE_MISMATCH, INVISIBLE_REFERENCE, NO_COMPANION_OBJECT!>C<!>

private konst test2: <!INVISIBLE_REFERENCE!>TA<!> = <!INVISIBLE_REFERENCE!>TA<!>()
private konst test2co = <!INVISIBLE_REFERENCE!>TA<!>

// FILE: file2.kt
private konst test1: C = C()
private konst test1co: C.Companion = <!INITIALIZER_TYPE_MISMATCH, NO_COMPANION_OBJECT!>C<!>

private konst test2: TA = <!INVISIBLE_REFERENCE!>TA<!>()
private konst test2co = TA

private class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>C<!>
private typealias <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>TA<!> = Int
