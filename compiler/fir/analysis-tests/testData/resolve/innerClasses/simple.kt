class Owner {

    fun foo() {
        bar()
        this.bar()
    }

    fun bar() {
        konst n = Nested()
        n.baz()
    }

    class Nested {
        fun baz() {
            gau()
            this.gau()
        }

        fun gau() {
            konst o = Owner()
            o.foo()
        }

        fun err() {
            <!UNRESOLVED_REFERENCE!>foo<!>()
            this.<!UNRESOLVED_REFERENCE!>foo<!>()
        }
    }
}

fun test() {
    konst o = Owner()
    o.foo()
    konst n = Owner.Nested()
    n.baz()
}
