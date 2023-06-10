class My {

    konst x = <!DEBUG_INFO_LEAKING_THIS!>foo<!>()

    konst w = bar()

    fun foo() = 0

    companion object {
        
        konst y = <!UNRESOLVED_REFERENCE!>foo<!>()

        konst u = <!DEBUG_INFO_LEAKING_THIS!>bar<!>()

        konst z: String? = bar()

        fun bar() = "1"
    }
}