// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE -UNREACHABLE_CODE

class A {
    class B {
        class C
    }
}

fun test(a: A.<!SYNTAX!><!>): A.<!SYNTAX!><!> {
    konst aa: A. <!SYNTAX!>=<!><!SYNTAX!><!> null!!
}

fun test1(a: A.B.<!SYNTAX!><!>): A.B.<!SYNTAX!><!> {
    konst aa: A.B. <!SYNTAX!>=<!><!SYNTAX!><!> null!!
}

fun test2(a: <!UNRESOLVED_REFERENCE!>A.e.C<!>): <!UNRESOLVED_REFERENCE!>A.e.C<!> {
    konst aa: <!UNRESOLVED_REFERENCE!>A.e.C<!> = null!!
}

fun test3(a: <!UNRESOLVED_REFERENCE!>a.A.C<!>): <!UNRESOLVED_REFERENCE!>a.A.C<!> {
    konst aa: <!UNRESOLVED_REFERENCE!>a.A.C<!> = null!!
}

fun test4(a: <!UNRESOLVED_REFERENCE!>A.B.ee<!>): <!UNRESOLVED_REFERENCE!>A.B.ee<!> {
    konst aa: <!UNRESOLVED_REFERENCE!>A.B.ee<!> = null!!
}

fun test5(a: <!UNRESOLVED_REFERENCE!>A.ee<!>): <!UNRESOLVED_REFERENCE!>A.ee<!> {
    konst aa: <!UNRESOLVED_REFERENCE!>A.ee<!> = null!!
}
