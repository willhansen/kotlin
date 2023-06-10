// !DIAGNOSTICS: -UNUSED_PARAMETER
// SKIP_TXT


// TESTCASE NUMBER: 1
class Case1<T>() {
    class A(konst t: <!UNRESOLVED_REFERENCE!>T<!>)
    class B(konst x: List<<!UNRESOLVED_REFERENCE!>T<!>>)
    class C(konst c: () -> <!UNRESOLVED_REFERENCE!>T<!>)
    class E(konst n: Nothing, konst t: <!UNRESOLVED_REFERENCE!>T<!>)
}
