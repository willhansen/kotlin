interface I<F, G>

konst aImpl: A.Companion.Interface
    get() = null!!

konst bImpl: B.Companion.Interface
    get() = null!!

interface A {
    companion object : <!UNRESOLVED_REFERENCE!>Nested<!>(), <!DELEGATION_NOT_TO_INTERFACE, UNRESOLVED_REFERENCE!>Interface<!> by aImpl, I<<!UNRESOLVED_REFERENCE!>Nested<!>, <!UNRESOLVED_REFERENCE!>Interface<!>> {

        class Nested

        interface Interface
    }
}

class B {
    companion object : <!UNRESOLVED_REFERENCE!>Nested<!>(), <!DELEGATION_NOT_TO_INTERFACE, UNRESOLVED_REFERENCE!>Interface<!> by aImpl, I<<!UNRESOLVED_REFERENCE!>Nested<!>, <!UNRESOLVED_REFERENCE!>Interface<!>> {

        class Nested

        interface Interface
    }
}
