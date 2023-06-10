// !DIAGNOSTICS: -UNUSED_VARIABLE

fun example() {
    konst a = if (true) true else false
    konst b = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) else false
    konst c = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) true
    konst d = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) true else;
    konst e = if (true) <!IMPLICIT_CAST_TO_ANY!>{}<!> else <!IMPLICIT_CAST_TO_ANY!>false<!>
    konst f = if (true) <!IMPLICIT_CAST_TO_ANY!>true<!> else <!IMPLICIT_CAST_TO_ANY!>{}<!>

    {
        if (true) true
    }();

    {
        if (true) true else false
    }();

    {
        if (true) {} else false
    }();


    {
        if (true) true else {}
    }()

    fun t(): Boolean {
        return <!TYPE_MISMATCH!><!INVALID_IF_AS_EXPRESSION!>if<!> (true) true<!>
    }

    return <!TYPE_MISMATCH!>if (true) true else {}<!>
}
