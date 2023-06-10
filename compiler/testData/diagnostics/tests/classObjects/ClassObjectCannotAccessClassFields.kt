// FIR_IDENTICAL
// http://youtrack.jetbrains.net/issue/KT-20

class A() {
    konst x = 1

    companion object {
        konst y = <!UNRESOLVED_REFERENCE!>x<!>
    }
}