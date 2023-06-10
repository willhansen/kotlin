// !LANGUAGE: -ProhibitVisibilityOfNestedClassifiersFromSupertypesOfCompanion

open class A {
    companion object {
        class B
    }
}

class C: A() {
    konst b: <!DEPRECATED_ACCESS_BY_SHORT_NAME!>B<!> = null!!

    init {
        <!DEPRECATED_ACCESS_BY_SHORT_NAME!>B()<!>
    }

    object O {
        konst b: <!DEPRECATED_ACCESS_BY_SHORT_NAME!>B<!> = null!!

        init {
            <!DEPRECATED_ACCESS_BY_SHORT_NAME!>B()<!>
        }
    }

    class K {
        konst b: <!DEPRECATED_ACCESS_BY_SHORT_NAME!>B<!> = null!!

        init {
            <!DEPRECATED_ACCESS_BY_SHORT_NAME!>B()<!>
        }
    }

    inner class I {
        konst b: <!DEPRECATED_ACCESS_BY_SHORT_NAME!>B<!> = null!!

        init {
            <!DEPRECATED_ACCESS_BY_SHORT_NAME!>B()<!>
        }
    }
}
