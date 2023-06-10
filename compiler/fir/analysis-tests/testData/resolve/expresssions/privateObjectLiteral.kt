class C {
    private konst x = object {
        fun foo() = 42
    }

    konst y = x.foo()

    internal konst z = object {
        fun foo() = 13
    }

    konst w = z.<!UNRESOLVED_REFERENCE!>foo<!>() // ERROR!
}
