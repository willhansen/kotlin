// FILE: file1.kt
private class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>C<!> {
    companion object
}

private typealias <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>TA<!> = C

private konst test1: C = C()
private konst test1co: C.Companion = C

private konst test2: TA = TA()
private konst test2co = TA

// FILE: file2.kt
private konst test1: <!INVISIBLE_REFERENCE!>C<!> = <!INVISIBLE_MEMBER!>C<!>()
private konst test1co: <!INVISIBLE_REFERENCE!>C<!>.<!INVISIBLE_REFERENCE!>Companion<!> = <!INVISIBLE_MEMBER!>C<!>

private konst test2: <!INVISIBLE_REFERENCE!>TA<!> = <!INVISIBLE_MEMBER!>TA<!>()
private konst test2co = <!INVISIBLE_MEMBER!>TA<!>

private class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>C<!>
private typealias <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>TA<!> = Int
