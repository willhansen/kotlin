// !DIAGNOSTICS: -UNUSED_VARIABLE

fun example() {
    konst a = if (true) true else false
    konst b = if (true) else false
    konst c = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) true
    konst d = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) true else;
    konst e = if (true) {} else false
    konst f = if (true) true else {}

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
        return <!INVALID_IF_AS_EXPRESSION!>if<!> (true) true
    }

    return <!RETURN_TYPE_MISMATCH!>if (true) true else {}<!>
}
