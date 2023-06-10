fun <!IMPLICIT_NOTHING_RETURN_TYPE!>foo<!>() = throw Exception()

fun <!IMPLICIT_NOTHING_RETURN_TYPE!>bar<!>() = null!!

fun <!IMPLICIT_NOTHING_RETURN_TYPE!>baz<!>() = bar()

fun gav(): Any = null!!

konst <!IMPLICIT_NOTHING_PROPERTY_TYPE!>x<!> = null!!

konst y: Nothing = throw Exception()

fun check() {
    // Error: KT-10449
    fun <!IMPLICIT_NOTHING_RETURN_TYPE!>local<!>() = bar()
    // Unreachable / unused, but not implicit Nothing
    <!UNREACHABLE_CODE!>konst x =<!> null!!
}

fun <!IMPLICIT_NOTHING_RETURN_TYPE!>nonLocalReturn<!>() = run { <!RETURN_TYPE_MISMATCH!>return<!> }

class Klass {
    fun <!IMPLICIT_NOTHING_RETURN_TYPE!>bar<!>() = null!!

    konst <!IMPLICIT_NOTHING_PROPERTY_TYPE!>y<!> = null!!

    init {
        fun <!IMPLICIT_NOTHING_RETURN_TYPE!>local<!>() = bar()
        // Should be unreachable: see KT-5311
        konst z = null!!
    }

    fun foo() {
        fun <!IMPLICIT_NOTHING_RETURN_TYPE!>local<!>() = bar()

        <!UNREACHABLE_CODE!>konst x =<!> y
    }
}

interface Base {
    konst x: Int

    fun foo(): String
}

class Derived : Base {
    // Ok for override

    override konst x = null!!

    override fun foo() = null!!
}
