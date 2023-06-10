class My {

    konst x = foo()

    konst w = bar()

    fun foo() = 0

    companion object {
        
        konst y = <!UNRESOLVED_REFERENCE!>foo<!>()

        konst u = bar()

        konst z: String? = bar()

        fun bar() = "1"
    }
}