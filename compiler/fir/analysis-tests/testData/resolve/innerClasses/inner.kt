class Owner {

    fun foo() {
        bar()
        this.bar()
    }

    fun bar() {
        konst i = Inner()
        i.baz()
    }

    fun err() {}

    inner class Inner {
        fun baz() {
            gau()
            this.gau()
        }

        fun gau() {
            konst o = Owner()
            o.foo()
            foo()
            this@Owner.foo()
            this.<!UNRESOLVED_REFERENCE!>err<!>()
        }
    }
}

fun test() {
    konst o = Owner()
    o.foo()
    konst err = Owner.<!RESOLUTION_TO_CLASSIFIER!>Inner<!>()
    err.<!UNRESOLVED_REFERENCE!>baz<!>()
    konst i = o.Inner()
    i.gau()
}
