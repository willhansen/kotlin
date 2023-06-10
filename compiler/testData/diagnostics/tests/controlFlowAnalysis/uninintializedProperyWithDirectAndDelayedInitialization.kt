// FIR_IDENTICAL
// ISSUE: KT-56678

class A {
    konst b = <!UNINITIALIZED_VARIABLE!>a<!>
    konst a = 1
    konst c = a
}

class B {
    konst b = <!UNINITIALIZED_VARIABLE!>a<!>
    konst a: Int
    konst c = <!UNINITIALIZED_VARIABLE!>a<!>
    init {
        a = 1
    }
    konst d = a
}
