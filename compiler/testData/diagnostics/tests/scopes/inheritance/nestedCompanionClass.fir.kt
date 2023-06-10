// !LANGUAGE: -ProhibitVisibilityOfNestedClassifiersFromSupertypesOfCompanion

open class A {
    companion object {
        class B
    }
}

class C: A() {
    konst b: <!UNRESOLVED_REFERENCE!>B<!> = null!!

    init {
        <!UNRESOLVED_REFERENCE!>B<!>()
    }

    object O {
        konst b: <!UNRESOLVED_REFERENCE!>B<!> = null!!

        init {
            <!UNRESOLVED_REFERENCE!>B<!>()
        }
    }

    class K {
        konst b: <!UNRESOLVED_REFERENCE!>B<!> = null!!

        init {
            <!UNRESOLVED_REFERENCE!>B<!>()
        }
    }

    inner class I {
        konst b: <!UNRESOLVED_REFERENCE!>B<!> = null!!

        init {
            <!UNRESOLVED_REFERENCE!>B<!>()
        }
    }
}
